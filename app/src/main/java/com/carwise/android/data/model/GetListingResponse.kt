package com.carwise.android.data.model


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetListingResponse(
    val id: String,
    val slug: String,
    val status: Int,
    val brand: Brand,
    val series: Series,
    val model: Model,
    val title: String,
    val description: String,
    val currency: String,
    val price: Long,
    val city: String,
    val district: String,
    val neighborhood: String,
    @SerializedName("is_favorite")
    val isFavorite:Boolean,
    val images: List<Image>,
    val detail: Detail,
    @SerializedName("created_by")
    val createdBy: UserInfo,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("updated_at")
    val updatedAt: Long,
)

@Serializable
data class UserInfo(
    val id: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
)

@Serializable
data class Series(
    val id: String,
    @SerializedName("brand_id")
    val brandId: String,
    val name: String,
)

@Serializable
data class Model(
    val id: String,
    @SerializedName("brand_id")
    val brandId: String,
    @SerializedName("series_id")
    val seriesId: String,
    val name: String,
)