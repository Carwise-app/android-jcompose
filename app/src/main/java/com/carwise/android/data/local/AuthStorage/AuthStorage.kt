package com.carwise.android.data.local.AuthStorage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carwise.android.data.model.LoginResponse
import com.carwise.android.data.model.UserPayload
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val gson = Gson()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

     fun saveLoginResponse(loginResponse: LoginResponse) {
        encryptedSharedPreferences.edit().apply {
            putString("access_token", loginResponse.access_token)
            try {
                val payload = decodeJwtPayload(loginResponse.access_token)
                putString("current_user_payload", gson.toJson(payload))
            } catch (e: Exception) {
            }
            apply()
        }
    }

     fun getAccessToken(): String {
        return encryptedSharedPreferences.getString("access_token", "") ?: ""
    }

     fun getCurrentUserPayload(): UserPayload? {
        val payloadJson = encryptedSharedPreferences.getString("current_user_payload", null)
        return try {
            payloadJson?.let { gson.fromJson(it, UserPayload::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    fun clearAuthData() {
        encryptedSharedPreferences.edit().clear().apply()
    }

    private fun decodeJwtPayload(token: String): UserPayload {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT token")
        }

        val payload = parts[1]
        val decodedPayload = android.util.Base64.decode(
            payload.replace('-', '+').replace('_', '/'),
            android.util.Base64.DEFAULT
        )
        val payloadString = String(decodedPayload, Charsets.UTF_8)
        return gson.fromJson(payloadString, UserPayload::class.java)
    }
}