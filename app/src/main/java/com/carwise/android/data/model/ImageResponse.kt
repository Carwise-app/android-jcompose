package com.carwise.android.data.model


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class ImageResponse(
    val image: Image,
)

@Serializable
data class Image(
    val id: String,
    val path: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("created_at")
    val createdAt: Long,
)

@Serializable
data class PredictionResponse(
    val prediction: Prediction,
)
@Serializable
data class Prediction(
    val image: Image,
    val prediction: Boolean,
    val confidence: Double,
    @SerializedName("created_at")
    val createdAt: Long,
)