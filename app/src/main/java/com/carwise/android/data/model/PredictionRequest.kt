package com.carwise.android.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class  PricePredictionRequest(
    @SerializedName("Marka")
    val marka: String,
    @SerializedName("Seri")
    val seri: String,
    @SerializedName("Model")
    val model: String,
    @SerializedName("Yıl")
    val yl: Long,
    @SerializedName("Kilometre")
    val kilometre: Double,
    @SerializedName("Motor_Hacmi")
    val motorHacmi: Double,
    @SerializedName("Motor_Gücü")
    val motorGucu: Double,
    @SerializedName("Tramer")
    val tramer: Double,
    @SerializedName("Boyalı_sayısı")
    val boyalSayisi: Long,
    @SerializedName("Değişen_sayısı")
    val degisenSayisi: Long,
    @SerializedName("Orjinal_sayısı")
    val orjinalSayisi: Long,
    @SerializedName("Vites_Tipi")
    val vitesTipi: String,
    @SerializedName("Yakıt_Tipi")
    val yakitTipi: String,
    @SerializedName("Kasa_Tipi")
    val kasaTipi: String,
    @SerializedName("Renk")
    val renk: String,
)

@Serializable
data class PricePredictionResponse(
    @SerializedName("tahmini_fiyat")
    val tahminiFiyat: Double,
    @SerializedName("r2_skoru")
    val r2Skoru: Double,
    val mae: Double,
)


@Serializable
data class GetPricePredictHistoryResponse(
    val predicts: List<PricePredict>,
    val total: Long,
)

@Serializable
data class PricePredict(
    val id: String,
    @SerializedName("created_by")
    val createdBy: String,
    val brand: String,
    val series: String,
    val model: String,
    val year: Long,
    val mileage: Long,
    @SerializedName("engine_volume")
    val engineVolume: Double,
    @SerializedName("engine_power")
    val enginePower: Long,
    @SerializedName("accident_history")
    val accidentHistory: Long,
    @SerializedName("transmission_type")
    val transmissionType: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("body_type")
    val bodyType: String,
    val color: String,
    @SerializedName("original_parts")
    val originalParts: Long,
    @SerializedName("replaced_parts")
    val replacedParts: Long,
    @SerializedName("painted_parts")
    val paintedParts: Long,
    val price: Double,
    val r2: Double,
    val mae: Double,
    @SerializedName("created_at")
    val createdAt: Long,
)
