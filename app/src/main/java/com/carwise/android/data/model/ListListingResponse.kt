package com.carwise.android.data.model


import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

@Serializable
data class ListListingResponse(
    val listings: List<Listing>,
    val total: Int,
)

@Serializable
data class Listing(
    val id: String,
    val slug: String,
    val status: Int,
    val brand: Brand,
    val series: Series,
    val model: Model,
    val title: String,
    val currency: String,
    val price: Long,
    val city: String,
    val district: String,
    val neighborhood: String,
    val image: Image,
    @SerializedName("created_at")
    val createdAt: Long,
)