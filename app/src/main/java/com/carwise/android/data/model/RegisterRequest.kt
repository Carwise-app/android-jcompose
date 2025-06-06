package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)