package com.carwise.android.data.model

data class UserPayload(
    val user_id: String,
    val email: String,
    val role: Int,
    val status: Int,
    val exp: Long,
    val first_name:String,
    val last_name:String,
    val country_code:String,
    val phone_number:String,
    val jti: String,
    val iat: Long,
    val iss: String
) 