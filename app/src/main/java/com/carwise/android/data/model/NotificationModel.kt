package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class SetGetNotificationRequest(
    @SerializedName("device_token")
    val deviceToken: String,
    @SerializedName("email_notify")
    val emailNotify: Boolean,
    @SerializedName("push_notify")
    val pushNotify: Boolean,
)
