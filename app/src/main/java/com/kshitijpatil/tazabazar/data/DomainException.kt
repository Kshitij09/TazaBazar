package com.kshitijpatil.tazabazar.data

sealed interface DomainException

sealed interface LoginException : DomainException
object InvalidCredentialsException : LoginException
object ValidationException : LoginException
object UnknownLoginException : LoginException