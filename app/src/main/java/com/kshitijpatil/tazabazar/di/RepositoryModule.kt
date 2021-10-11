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
import com.kshitijpatil.tazabazar.util.NetworkUtils
import com.squareup.moshi.Moshi
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


    fun provideProductRepository(context: Context, okhttpClient: OkHttpClient): ProductRepository {
        synchronized(lock) {
            return productRepository ?: createProductRepository(context, okhttpClient)
        }
    }

    fun createProductRepository(context: Context, okhttpClient: OkHttpClient): ProductRepository {
        val appDatabase = database ?: createDatabase(context)
        val api = ApiModule.provideProductApi(okhttpClient)
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

    fun createCartRepository(context: Context): CartRepository {
        val appDatabase = database ?: createDatabase(context)
        val mapper = MapperModule.cartItemDetailViewToCartItem
        val repo = CartRepositoryImpl(appDatabase.cartItemDao, mapper)
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

    fun createDatabase(context: Context): AppDatabase {
        val result =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.databaseName)
                .fallbackToDestructiveMigration()
                .build()
        database = result
        return result
    }

    fun provideNetworkUtils(context: Context): NetworkUtils = NetworkUtils(context)

    fun provideProductCacheExpiryMillis(): Long = 30 * 60 * 1000L // 30 minutes

    fun provideAuthRepository(context: Context, client: OkHttpClient): AuthRepository {
        synchronized(lock) {
            return authRepository ?: createAuthRepository(context, client)
        }
    }

    fun provideRegisterRepository(client: OkHttpClient): RegisterRepository {
        synchronized(lock) {
            return registerRepository ?: createRegisterRepository(client)
        }
    }

    fun provideLoginRepository(context: Context, client: OkHttpClient): LoginRepository {
        synchronized(lock) {
            return loginRepository ?: createLoginRepository(context, client)
        }
    }

    private fun createAuthRepository(context: Context, client: OkHttpClient): AuthRepository {
        val newRepo = AuthRepositoryImpl(
            provideRegisterRepository(client),
            provideLoginRepository(context, client),
            provideAuthRemoteDataSource(client),
            provideAuthPreferenceStore(context),
            appDispatchers
        )
        authRepository = newRepo
        return newRepo
    }

    private fun createRegisterRepository(client: OkHttpClient): RegisterRepository {
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

    private fun createLoginRepository(context: Context, client: OkHttpClient): LoginRepository {
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