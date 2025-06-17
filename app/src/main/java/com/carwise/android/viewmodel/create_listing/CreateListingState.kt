package com.carwise.android.viewmodel.create_listing

import com.carwise.android.data.model.Brand
import com.carwise.android.data.model.GetBrandSeries
import com.carwise.android.data.model.GetBrandSeriesModels
import com.carwise.android.data.model.District
import android.net.Uri

data class CreateListingState(
    // Brands, Series, Models from API
    val brands: List<Brand> = emptyList(),
    val selectedBrand: Brand? = null,
    val selectedSeries: GetBrandSeries? = null,
    val selectedModel: GetBrandSeriesModels? = null,
    
    // Vehicle Details
    val selectedYear: Int? = null,
    val selectedBodyType: String? = null,
    val selectedFuelType: String? = null,
    val selectedTransmissionType: String? = null,
    val selectedDriveType: String? = null,
    val selectedColor: String? = null,
    val selectedHeavyDamage: Boolean? = null,
    
    // Location
    val selectedCity: String? = null,
    val selectedDistrict: District? = null,
    val selectedNeighborhood: String? = null,
    
    // Price and Images
    val selectedPrice: Long? = null,
    val selectedCurrency: String = "TL",
    val selectedImages: List<String> = emptyList(),
    val title: String = "",
    
    // Additional Details
    val selectedEnginePower: Long? = null,
    val selectedEngineVolume: Long? = null,
    val selectedKilometers: Long? = null,
    
    // Damage Details
    val frontBumper: String = "Orijinal",
    val frontHood: String = "Orijinal",
    val roof: String = "Orijinal",
    val frontRightDoor: String = "Orijinal",
    val rearRightDoor: String = "Orijinal",
    val frontLeftMudguard: String = "Orijinal",
    val frontLeftDoor: String = "Orijinal",
    val rearLeftDoor: String = "Orijinal",
    val rearLeftMudguard: String = "Orijinal",
    val rearBumper: String = "Orijinal",
    
    // UI State
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val description: String = "",
    val isBrandSelected: Boolean = false
) 