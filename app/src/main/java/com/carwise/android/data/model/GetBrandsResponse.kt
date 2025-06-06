package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GetBrandsResponse(
    val brands: List<Brand>
)

@Serializable
data class Brand(
    val id: String,
    @SerializedName("image_path")
    val imagePath: String?,
    val name: String,
    val series: List<GetBrandSeries>
)

@Serializable
data class GetBrandSeries(
    val id: String,
    val models: List<GetBrandSeriesModels>,
    val name: String
)

@Serializable
data class GetBrandSeriesModels(
    @SerializedName("brand_id")
    val brandId: String,
    val id: String,
    val name: String
)
