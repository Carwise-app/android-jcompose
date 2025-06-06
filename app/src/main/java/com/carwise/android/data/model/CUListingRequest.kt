package com.carwise.android.data.model


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CreateListingRequest(
    @SerializedName("brand_id")
    val brandId: String,
    @SerializedName("series_id")
    val seriesId: String,
    @SerializedName("model_id")
    val modelId: String,
    val title: String,
    val description: String,
    val currency: String,
    val price: Long,
    val city: String,
    val district: String,
    val neighborhood: String,
    val images: List<String>,
    val detail: Detail,
)

@Serializable
data class Detail(
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("transmission_type")
    val transmissionType: String,
    @SerializedName("body_type")
    val bodyType: String,
    @SerializedName("drive_type")
    val driveType: String,
    @SerializedName("engine_power")
    val enginePower: Long,
    @SerializedName("engine_volume")
    val engineVolume: Long,
    val kilometers: Long,
    val year: Long,
    val color: String,
    @SerializedName("heavy_damage")
    val heavyDamage: Boolean,
    @SerializedName("front_bumper")
    val frontBumper: String,
    @SerializedName("front_hood")
    val frontHood: String,
    val roof: String,
    @SerializedName("front_right_door")
    val frontRightDoor: String,
    @SerializedName("rear_right_door")
    val rearRightDoor: String,
    @SerializedName("front_left_mudguard")
    val frontLeftMudguard: String,
    @SerializedName("front_left_door")
    val frontLeftDoor: String,
    @SerializedName("rear_left_door")
    val rearLeftDoor: String,
    @SerializedName("rear_left_mudguard")
    val rearLeftMudguard: String,
    @SerializedName("rear_bumper")
    val rearBumper: String,
)

@Serializable
data class IdResponse(
    val id: String,
)

@Serializable
data class UpdateListingStatusRequest(
    val status:Int
)