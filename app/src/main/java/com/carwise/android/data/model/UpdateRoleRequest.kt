package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoleRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("role")
    val role: Int,
)