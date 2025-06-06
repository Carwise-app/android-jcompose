package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetChatResponse(
    val chats: List<Chat>,
    val total: Long,
)

@Serializable
data class Chat(
    val user: UserInfo,
    val listing: Listing,
    @SerializedName("last_message_time")
    val lastMessageTime: Long,
)
