package com.kshitijpatil.tazabazar.ui.auth

import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.UiState

data class AuthViewState(
    val username: String? = null,
    val password: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val loginState: UiState<LoggedInUser> = UiState.Idle,
    val registerState: UiState<LoggedInUser> = UiState.Idle
)