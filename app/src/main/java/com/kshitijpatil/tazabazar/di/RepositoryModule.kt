package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.api.dto.ApiError
import com.kshitijpatil.tazabazar.data.*
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.local.ProductLocalDataSource
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStoreImpl
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import com.kshitijpatil.tazabazar.data.mapper.ErrorBodyDecoder
import com.kshitijpatil.tazabazar.data.mapper.LocalDateTimeSerializer
import com.kshitijpatil.tazabazar.data.mapper.LoggedInUserSerializer
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSourceImpl
import com.kshitijpatil.tazabazar.data.network.ProductRemoteDataSource
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.NetworkUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDateTime

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()
    private val moshi = Moshi.Builder().build()
    private val lock = Any()
    private var database: AppDatabase? = null
    private val apiErrorMapper by lazy {
        ErrorBodyDecoder(moshi.adapter(ApiError::class.java))
    }
    private val loggedInUserJsonAdapter by lazy {
        moshi.adapter(LoggedInUser::class.java)
    }

    @Volatile
    var productRepository: ProductRepository? = null
        @VisibleForTesting set

    @Volatile
    var cartRepository: CartRepository? = null
        @VisibleForTesting set

    @Volatile
    var registerRepository: RegisterRepository? = null
        @VisibleForTesting set

    @Volatile
    var loginRepository: LoginRepository? = null
        @VisibleForTesting set

    @Volatile
    var authRepository: AuthRepository? = null
        @VisibleForTesting set

    @Volatile
    var orderRepository: OrderRepository? = null
        @VisibleForTesting set

    fun provideProductRepository(context: Context): ProductRepository {
        synchronized(lock) {
            return productRepository ?: createProductRepository(context)
        }
    }

    private fun createProductRepository(context: Context): ProductRepository {
        val appDatabase = database ?: createDatabase(context)
        val client = OkhttpModule.provideOkHttpClient(context)
        val api = ApiModule.provideProductApi(client)
        val networkUtils = provideNetworkUtils(context)
        val newRepo = ProductRepositoryImpl(
            provideRemoteDataSource(api),
            provideLocalDataSource(appDatabase),
            networkUtils,
            appDatabase,
            appDispatchers,
            MapperModule.productToProductWithInventories,
            MapperModule.productWithInventoriesToProduct,
            MapperModule.productCategoryToProductCategoryEntity
        )
        productRepository = newRepo
        return newRepo
    }

    fun provideCartItemRepository(context: Context): CartRepository {
        synchronized(lock) {
            return cartRepository ?: createCartRepository(context)
        }
    }

    private fun createCartRepository(context: Context): CartRepository {
        val appDatabase = database ?: createDatabase(context)
        val mapper = MapperModule.cartItemDetailViewToCartItem
        val dispatchers = AppModule.provideAppCoroutineDispatchers()
        val repo = CartRepositoryImpl(appDatabase.cartItemDao, dispatchers, mapper)
        cartRepository = repo
        return repo
    }

    fun provideLocalDataSource(appDatabase: AppDatabase): ProductDataSource {
        return ProductLocalDataSource(
            favoriteDao = appDatabase.favoriteDao,
            productMapper = MapperModule.productWithInventoriesAndFavoritesToProduct,
            productCategoryDao = appDatabase.productCategoryDao
        )
    }

    fun provideRemoteDataSource(api: ProductApi): ProductDataSource {
        return ProductRemoteDataSource(
            productApi = api,
            categoryMapper = MapperModule.productCategoryDtoToProductCategory,
            productMapper = MapperModule.productResponseToProduct
        )
    }

    private fun createDatabase(context: Context): AppDatabase {
        val result =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.databaseName)
                .fallbackToDestructiveMigration()
                .build()
        database = result
        return result
    }

    fun provideNetworkUtils(context: Context): NetworkUtils = NetworkUtils(context)

    fun provideProductCacheExpiryMillis(): Long = 30 * 60 * 1000L // 30 minutes

    fun provideAuthRepository(context: Context): AuthRepository {
        synchronized(lock) {
            return authRepository ?: createAuthRepository(context)
        }
    }

    fun provideRegisterRepository(context: Context): RegisterRepository {
        synchronized(lock) {
            return registerRepository ?: createRegisterRepository(context)
        }
    }

    fun provideLoginRepository(context: Context): LoginRepository {
        synchronized(lock) {
            return loginRepository ?: createLoginRepository(context)
        }
    }

    fun provideOrderRepository(
        context: Context,
        externalScope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers
    ): OrderRepository {
        return orderRepository ?: createOrderRepository(context, externalScope, dispatchers)
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        val client = OkhttpModule.provideOkHttpClient(context)
        val newRepo = AuthRepositoryImpl(
            provideRegisterRepository(context),
            provideLoginRepository(context),
            provideAuthRemoteDataSource(client),
            provideAuthPreferenceStore(context),
            appDispatchers
        )
        authRepository = newRepo
        return newRepo
    }

    private fun createRegisterRepository(context: Context): RegisterRepository {
        val client = OkhttpModule.provideOkHttpClient(context)
        val authRemoteDataSource = provideAuthRemoteDataSource(client)
        val newRepo = RegisterRepositoryImpl(
            authRemoteDataSource,
            apiErrorMapper,
            MapperModule.loggedInUserMapper,
            appDispatchers
        )
        registerRepository = newRepo
        return newRepo
    }

    private fun createLoginRepository(context: Context): LoginRepository {
        val client = OkhttpModule.provideOkHttpClient(context)
        val authRemoteDataSource = provideAuthRemoteDataSource(client)
        val newRepo = LoginRepositoryImpl(
            authRemoteDataSource,
            appDispatchers,
            MapperModule.loggedInUserMapper,
            provideAuthPreferenceStore(context)
        )
        loginRepository = newRepo
        return newRepo
    }

    private fun createOrderRepository(
        context: Context,
        externalScope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers
    ): OrderRepository {
        val client = OkhttpModule.provideOkHttpClient(context)
        val orderApiFactory = provideOrderApiFactory(client)
        val authPreferenceStore = provideAuthPreferenceStore(context)
        val database = provideAppDatabase(context)
        val repo = OrderRepositoryImpl(
            externalScope,
            dispatchers,
            orderApiFactory,
            MapperModule.orderMapper,
            database.inventoryDao,
            authPreferenceStore
        )
        orderRepository = repo
        return repo
    }

    fun provideAppDatabase(context: Context): AppDatabase {
        return database ?: createDatabase(context)
    }

    fun provideOrderApiFactory(client: OkHttpClient): OrderApiFactory {
        return DefaultOrderApiFactory(client)
    }

    fun provideAuthRemoteDataSource(client: OkHttpClient): AuthRemoteDataSource {
        return AuthRemoteDataSourceImpl(ApiModule.provideAuthApi(client))
    }

    fun provideAuthPreferenceStore(context: Context): AuthPreferenceStore {
        return AuthPreferenceStoreImpl(
            PreferenceStorageModule.providePreferenceStorage(context),
            provideLocalDateTimeSerializer(),
            provideLoggedInUserSerializer(),
            appDispatchers.io
        )
    }

    fun provideLocalDateTimeSerializer(): EitherStringSerializer<LocalDateTime> {
        return LocalDateTimeSerializer()
    }

    fun provideLoggedInUserSerializer(): EitherStringSerializer<LoggedInUser> {
        return LoggedInUserSerializer(loggedInUserJsonAdapter)
    }
}