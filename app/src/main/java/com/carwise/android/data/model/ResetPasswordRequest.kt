package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordQuery(
    val token: String,
    val email: String
)
@Serializable
data class ResetPasswordRequest(
    val password: String,
    @SerializedName("re_password")
    val rePassword: String,
)