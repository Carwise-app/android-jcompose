package com.carwise.android.data.model

data class ListingFilters(
    // Search
    val query: String = "",
    
    // Brand and Model
    val brandId: String = "",
    val seriesId: String = "",
    val modelId: String = "",
    
    // Vehicle Details
    val bodyType: String = "",
    val driveType: String = "",
    val transmissionType: String = "",
    val fuelType: String = "",
    val color: String = "",
    val heavyDamage: Boolean? = null,
    
    // Location
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    
    // Price and Year
    val priceRange: Pair<Int, Int> = Pair(0, 0),
    val yearRange: Pair<Int, Int> = Pair(0, 0),
    
    // Additional Details
    val kilometersRange: Pair<Int, Int> = Pair(0, 0),
    val enginePowerRange: Pair<Int, Int> = Pair(0, 0),
    val engineVolumeRange: Pair<Int, Int> = Pair(0, 0),
    
    // Sorting
    val sortBy: SortBy = SortBy.CREATED_AT,
    val sortOrder: SortOrder = SortOrder.DESC,
    
    // Status
    val status: String = "",
    val createdBy: String = ""
)

enum class SortBy(val value: String) {
    PRICE("price"),
    YEAR("year"),
    KILOMETERS("kilometers"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at")
}

enum class SortOrder(val value: String) {
    ASC("asc"),
    DESC("desc")
} 