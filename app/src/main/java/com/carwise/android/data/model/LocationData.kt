package com.carwise.android.data.model

data class LocationData(
    val cities: List<City> = emptyList()
)

data class City(
    val name: String,
    val districts: List<District> = emptyList()
)

data class District(
    val id: Int,
    val name: String,
    val neighborhoods: List<String> = emptyList()
) 