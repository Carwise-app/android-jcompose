package com.carwise.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val access_token: String,
)
