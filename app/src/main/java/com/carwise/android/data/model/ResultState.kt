package com.carwise.android.data.model

import kotlinx.serialization.Serializable

sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val error: ErrorResponse) : ResultState<Nothing>()
}

@Serializable
data class ErrorResponse(
    val error: String
)
