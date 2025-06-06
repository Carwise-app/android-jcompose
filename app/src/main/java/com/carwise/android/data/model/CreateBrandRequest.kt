package com.carwise.android.data.model


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBrandRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("image_id")
    val imageId : String
)

@Serializable
data class NameRequest(
    @SerializedName("name")
    val name: String
)

