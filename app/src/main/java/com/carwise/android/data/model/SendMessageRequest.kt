package com.carwise.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val message: String,
)