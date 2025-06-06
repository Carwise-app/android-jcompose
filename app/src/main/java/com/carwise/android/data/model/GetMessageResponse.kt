package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetMessageResponse(
    val messages: List<Message>,
    val total: Long,
)
@Serializable
data class Message(
    val id: String,
    val sender: UserInfo,
    val receiver: UserInfo,
    val message: String,
    val read: Boolean,
    @SerializedName("created_at")
    val createdAt: Long,
)