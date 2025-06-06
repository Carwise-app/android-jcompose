package com.carwise.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String
) 