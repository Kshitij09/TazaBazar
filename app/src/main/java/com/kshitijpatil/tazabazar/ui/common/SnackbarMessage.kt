package com.kshitijpatil.tazabazar.ui.common

import androidx.annotation.StringRes

sealed class SnackbarMessage
data class ResourceMessage(@StringRes val resId: Int) : SnackbarMessage()
data class TextMessage(val text: String) : SnackbarMessage()