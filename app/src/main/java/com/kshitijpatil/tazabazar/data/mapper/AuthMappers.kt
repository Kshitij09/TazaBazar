package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.model.LoggedInUser

class LoginResponseUserToLoggedInUser : Mapper<LoginResponse.User, LoggedInUser> {
    override fun map(from: LoginResponse.User): LoggedInUser {
        return LoggedInUser(
            from.username,
            from.fullName,
            from.phone,
            from.emailVerified,
            from.phoneVerified
        )
    }

}