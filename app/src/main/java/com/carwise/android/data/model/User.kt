package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val email: String,
    val role: Int,
    val status: Int,
    @SerializedName("created_at")
    val createdAt: Long
) 