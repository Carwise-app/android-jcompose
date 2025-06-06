package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetNotificationResponse(
    val notifications: List<Notification>,
    val total: Long,
    val unread: Long,
)
@Serializable
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val status: Long,
    val data: Data,
    val read: Boolean,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("created_at")
    val createdAt: Long,
)
@Serializable
data class Data(
    val image: String?,
    val listing_id: String?,
    val user_id: String?,
    val user_name: String?,
)
