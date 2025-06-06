package com.carwise.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    @SerialName("id")
    val id: String,
    
    @SerialName("first_name")
    val first_name: String,
    
    @SerialName("last_name")
    val last_name: String,
    
    @SerialName("image_url")
    val image_url: String,
    
    @SerialName("country_code")
    val country_code: String,
    
    @SerialName("phone_number")
    val phone_number: String,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("role")
    val role: Int,
    
    @SerialName("status")
    val status: Int,
    
    @SerialName("created_at")
    val created_at: Long
)

