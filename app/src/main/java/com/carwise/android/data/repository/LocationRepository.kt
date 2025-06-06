package com.carwise.android.data.repository

import android.content.Context
import android.util.Log
import com.carwise.android.data.model.City
import com.carwise.android.data.model.District
import com.carwise.android.data.model.LocationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.sortBy
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class LocationRepository(private val context: Context) {
    private val gson = Gson()
    private var locationData: LocationData? = null
    private val TAG = "LocationRepository"
    private val client = OkHttpClient()
    private val baseUrl = "https://turkiyeapi.herokuapp.com/api/v1"

    suspend fun loadLocationData(): LocationData = withContext(Dispatchers.IO) {
        if (locationData != null) {
            Log.d(TAG, "Returning cached location data")
            return@withContext locationData!!
        }

        Log.d(TAG, "Starting to load location data from API")
        val cities = mutableListOf<City>()

        try {
            val request = Request.Builder()
                .url("$baseUrl/provinces")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected response ${response.code}")

                val jsonString = response.body?.string()
                val type = object : TypeToken<ApiResponse>() {}.type
                val apiResponse: ApiResponse = gson.fromJson(jsonString, type)

                apiResponse.data.forEach { province ->
                    val districts = province.districts.map { district ->
                        District(
                            id = district.id,
                            name = district.name,
                            neighborhoods = emptyList()
                        )
                    }.sortedBy { it.name }.toMutableList()
                    cities.add(City(province.name, districts))
                }
            }

            // İsimlere göre sırala
            val sortedCities = cities.sortedBy { it.name }
            Log.d(TAG, "Successfully loaded ${sortedCities.size} cities from API")
            LocationData(sortedCities).also { 
                locationData = it
                Log.d(TAG, "Location data cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading location data from API: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getCities(): List<String> = withContext(Dispatchers.IO) {
        try {
            val cities = loadLocationData().cities.map { it.name }
            Log.d(TAG, "Retrieved ${cities.size} cities")
            cities
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cities: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDistricts(cityName: String): List<District> = withContext(Dispatchers.IO) {
        try {
            val districts = loadLocationData().cities.find { it.name == cityName }?.districts ?: emptyList()
            Log.d(TAG, "Retrieved ${districts.size} districts for city: $cityName")
            districts.sortedBy { it.name }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting districts for $cityName: ${e.message}")
            emptyList()
        }
    }

    suspend fun getNeighborhoods(districtId: Int): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/districts/$districtId")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to get neighborhoods for district $districtId: ${response.code}")
                    return@withContext emptyList()
                }

                val jsonString = response.body?.string()
                val type = object : TypeToken<DistrictResponse>() {}.type
                val districtResponse: DistrictResponse = gson.fromJson(jsonString, type)

                districtResponse.data.neighborhoods.map { it.name }.sorted()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting neighborhoods for district $districtId: ${e.message}")
            emptyList()
        }
    }

    // API Response data classes
    private data class ApiResponse(
        val status: String,
        val data: List<Province>
    )

    private data class Province(
        val id: Int,
        val name: String,
        val population: Int,
        val area: Int,
        val altitude: Int,
        val areaCode: List<Int>,
        val isCoastal: Boolean,
        val isMetropolitan: Boolean,
        val coordinates: Coordinates,
        val maps: Maps,
        val region: Region,
        val districts: List<DistrictData>
    )

    private data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    private data class Maps(
        val googleMaps: String,
        val openStreetMap: String
    )

    private data class Region(
        val en: String,
        val tr: String
    )

    private data class DistrictData(
        val id: Int,
        val name: String,
        val population: Int,
        val area: Int
    )

    private data class DistrictResponse(
        val status: String,
        val data: DistrictDetail
    )

    private data class DistrictDetail(
        val provinceId: Int,
        val id: Int,
        val province: String,
        val name: String,
        val population: Int,
        val area: Int,
        val neighborhoods: List<Neighborhood>,
        val villages: List<Any>
    )

    private data class Neighborhood(
        val id: Int,
        val name: String,
        val population: Int
    )
} 