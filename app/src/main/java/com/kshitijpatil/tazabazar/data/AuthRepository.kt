package com.kshitijpatil.tazabazar.data

import arrow.core.Either
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.model.LoggedInUser

interface AuthRepository {
    suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser>
    suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser>
    suspend fun refreshToken(): Result<Unit>
}

class AuthRepositoryImpl(
    private val registerRepository: RegisterRepository,
    private val loginRepository: LoginRepository
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser> {
        return loginRepository.login(request)
    }

    override suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser> {
        return registerRepository.register(request)
    }

    override suspend fun refreshToken(): Result<Unit> {
        TODO("Not yet implemented")
    }
}