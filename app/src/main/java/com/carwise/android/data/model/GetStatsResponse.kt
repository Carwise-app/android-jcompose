package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetStatsResponse(
    @SerializedName("favorite_count")
    val favoriteCount: Int,
    @SerializedName("image_count")
    val imageCount: Int,
    @SerializedName("listing_count")
    val listingCount: Int,
    @SerializedName("message_count")
    val messageCount: Int,
    @SerializedName("notification_count")
    val notificationCount: Int,
    @SerializedName("predict_count")
    val predictCount: Int,
    @SerializedName("user_count")
    val userCount: Int,
)

@Serializable
data class GetUsersResponse(
    val total: Long,
    val users: List<UserResponse>,
)

@Serializable
data class UserResponse(
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("created_at")
    val createdAt: Long,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("google_id")
    val googleId: String,
    val id: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("last_login")
    val lastLogin: Long,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val role: Long,
    val status: Long,
    @SerializedName("updated_at")
    val updatedAt: Long,
)
