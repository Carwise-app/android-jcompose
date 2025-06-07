package com.carwise.android.viewmodel.listing

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.Brand
import com.carwise.android.data.model.District
import com.carwise.android.data.model.GetBrandSeries
import com.carwise.android.data.model.GetBrandSeriesModels
import com.carwise.android.data.model.Listing
import com.carwise.android.data.model.ListingFilters
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.SortBy
import com.carwise.android.data.model.SortOrder
import com.carwise.android.model.repository.CarwiseRepository
import com.carwise.android.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class ListingsState(
    // Listings
    val listings: List<Listing> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    
    // Filters
    val filters: ListingFilters = ListingFilters(),
    val activeFilters: List<String> = emptyList(),
    
    // Brands and Models
    val brands: List<Brand> = emptyList(),
    val selectedBrandSeries: List<GetBrandSeries> = emptyList(),
    val selectedSeriesModels: List<GetBrandSeriesModels> = emptyList(),
    
    // Loading States
    val isLoadingBrands: Boolean = false,
    val isLoadingSeries: Boolean = false,
    val isLoadingModels: Boolean = false,
    
    // Brand Selection State
    val selectedBrand: Brand? = null,
    val selectedSeries: GetBrandSeries? = null,
    val selectedModel: GetBrandSeriesModels? = null
)

data class ListingFilters(
    val brandId: String = "",
    val seriesId: String = "",
    val modelId: String = "",
    val priceRange: Pair<Int, Int> = Pair(0, Int.MAX_VALUE),
    val yearRange: Pair<Int, Int> = Pair(0, 2024),
    val fuelType: String = "",
    val transmissionType: String = "",
    val bodyType: String = "",
    val color: String = "",
    val kilometersRange: Pair<Int, Int> = Pair(0, Int.MAX_VALUE),
    val enginePowerRange: Pair<Int, Int> = Pair(0, Int.MAX_VALUE),
    val engineVolumeRange: Pair<Int, Int> = Pair(0, Int.MAX_VALUE),
    val driveType: String = "",
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val heavyDamage: Boolean? = null
)

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val repository: CarwiseRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListingsState())
    val state: StateFlow<ListingsState> = _state.asStateFlow()

    // Cache for brands
    private var cachedBrands: List<Brand>? = null

    private var currentPage = 1
    private var isInitialLoad = true
    private var isLoadingListings = false
    private var isRefreshing = false

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts: StateFlow<List<District>> = _districts.asStateFlow()

    private val _neighborhoods = MutableStateFlow<List<String>>(emptyList())
    val neighborhoods: StateFlow<List<String>> = _neighborhoods.asStateFlow()

    init {
        loadInitialData()
        loadCities()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Eğer cache'de varsa kullan
                if (cachedBrands != null) {
                    _state.update { 
                        it.copy(
                            brands = cachedBrands!!,
                            isLoadingBrands = false,
                            error = null
                        )
                    }
                    loadListings()
                    return@launch
                }

                _state.update { it.copy(isLoadingBrands = true, error = null) }
                when (val result = repository.getBrands()) {
                    is ResultState.Success -> {
                        // Cache'e kaydet
                        cachedBrands = result.data.brands.sortedBy { brand -> brand.name }
                        _state.update { 
                            it.copy(
                                brands = cachedBrands!!,
                                isLoadingBrands = false,
                                error = null
                            )
                        }
                        loadListings()
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoadingBrands = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoadingBrands = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Veriler yüklenirken bir hata oluştu: ${e.message}",
                        isLoadingBrands = false
                    )
                }
            }
        }
    }

    fun loadListings() {
        if (isLoadingListings || _state.value.isPaginating) return

        isLoadingListings = true
        viewModelScope.launch {
            try {
                if (isInitialLoad && !isRefreshing) {
                    _state.update { it.copy(isLoading = true, error = null) }
                } else if (!isRefreshing) {
                    _state.update { it.copy(isPaginating = true) }
                }
            
                val filters = _state.value.filters
                val result = repository.listListing(
                    query = filters.query.takeIf { it.isNotEmpty() },
                    brandId = filters.brandId,
                    seriesId = filters.seriesId,
                    modelId = filters.modelId,
                    bodyType = filters.bodyType.takeIf { it.isNotEmpty() },
                    driveType = filters.driveType.takeIf { it.isNotEmpty() },
                    transmissionType = filters.transmissionType.takeIf { it.isNotEmpty() },
                    fuelType = filters.fuelType.takeIf { it.isNotEmpty() },
                    city = filters.city.takeIf { it.isNotEmpty() },
                    district = filters.district.takeIf { it.isNotEmpty() },
                    neighborhood = filters.neighborhood.takeIf { it.isNotEmpty() },
                    minPrice = filters.priceRange.first.takeIf { it > 0 },
                    maxPrice = filters.priceRange.second.takeIf { it < Int.MAX_VALUE },
                    minYear = filters.yearRange.first.takeIf { it > 0 },
                    maxYear = filters.yearRange.second.takeIf { it < Int.MAX_VALUE },
                    minKilometers = filters.kilometersRange.first.takeIf { it > 0 },
                    maxKilometers = filters.kilometersRange.second.takeIf { it < Int.MAX_VALUE },
                    minEnginePower = filters.enginePowerRange.first.takeIf { it > 0 },
                    maxEnginePower = filters.enginePowerRange.second.takeIf { it < Int.MAX_VALUE },
                    minEngineVolume = filters.engineVolumeRange.first.takeIf { it > 0 },
                    maxEngineVolume = filters.engineVolumeRange.second.takeIf { it < Int.MAX_VALUE },
                    color = filters.color.takeIf { it.isNotEmpty() },
                    heavyDamage = filters.heavyDamage,
                    sortBy = filters.sortBy.value,
                    sortOrder = filters.sortOrder.value,
                    createdBy = filters.createdBy.takeIf { it.isNotEmpty() },
                    status = filters.status.takeIf { it.isNotEmpty() },
                    page = currentPage
                )

                when (result) {
                    is ResultState.Success -> {
                        val newListings = result.data.listings
                        _state.update { currentState ->
                            val combinedListings = if (currentPage == 1 || isRefreshing) {
                                newListings
                            } else {
                                currentState.listings + newListings
                            }
                            val uniqueListings = combinedListings.distinctBy { it.id }
                            currentState.copy(
                                listings = uniqueListings,
                                total = result.data.total,
                                filters = filters,
                                isLoading = false,
                                isPaginating = false,
                                error = null,
                                endReached = newListings.isEmpty()
                            )
                        }
                        if (newListings.isNotEmpty() && !isRefreshing) {
                            currentPage++
                        }
                    }
                    is ResultState.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isPaginating = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        // Loading state is handled by isLoading and isPaginating flags
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isPaginating = false,
                        error = "İlanlar yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            } finally {
                isLoadingListings = false
                isInitialLoad = false
                isRefreshing = false
            }
        }
    }

    fun loadMoreListings() {
        if (!_state.value.endReached && !isLoadingListings && !isRefreshing) {
            loadListings()
        }
    }

    fun refreshListings() {
        if (isLoadingListings) return
        
        isRefreshing = true
        currentPage = 1
        isInitialLoad = true
        loadListings()
    }

    fun selectBrand(brand: Brand) {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        selectedBrand = if (brand.id.isEmpty()) null else brand,
                        selectedSeries = null,
                        selectedModel = null,
                        filters = currentState.filters.copy(
                            brandId = brand.id,
                            seriesId = "",
                            modelId = ""
                        )
                    )
                }
                refreshListings()
            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error selecting brand: ${e.message}")
            }
        }
    }

    fun selectSeries(series: GetBrandSeries) {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        selectedSeries = if (series.id.isEmpty()) null else series,
                        selectedModel = null,
                        filters = currentState.filters.copy(
                            seriesId = series.id,
                            modelId = ""
                        )
                    )
                }
                refreshListings()
            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error selecting series: ${e.message}")
            }
        }
    }

    fun selectModel(model: GetBrandSeriesModels) {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        selectedModel = if (model.id.isEmpty()) null else model,
                        filters = currentState.filters.copy(
                            modelId = model.id
                        )
                    )
                }
                refreshListings()
            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error selecting model: ${e.message}")
            }
        }
    }

    fun clearBrandSelection() {
        viewModelScope.launch {
            try {
                // Önce model seçimini temizle
                _state.update {
                    it.copy(
                        selectedModel = null,
                        filters = it.filters.copy(modelId = "")
                    )
                }

                // Sonra seri seçimini temizle
                _state.update {
                    it.copy(
                        selectedSeries = null,
                        selectedSeriesModels = emptyList(),
                        filters = it.filters.copy(seriesId = "")
                    )
                }

                // En son marka seçimini temizle
                _state.update {
                    it.copy(
                        selectedBrand = null,
                        selectedBrandSeries = emptyList(),
                        filters = it.filters.copy(brandId = "")
                    )
                }

                // Listeyi yenile
                currentPage = 1
                isInitialLoad = true
                loadListings()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Seçimler temizlenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun applyFilters(filters: ListingFilters) {
        if (isLoadingListings) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Aktif filtreleri güncelle
                val activeFilters = mutableListOf<String>()

                // Marka, seri ve model filtrelerini ekle
                val selectedBrand = state.value.selectedBrand
                val selectedSeries = state.value.selectedSeries
                val selectedModel = state.value.selectedModel

                selectedBrand?.let { activeFilters.add("Marka: ${it.name}") }
                selectedSeries?.let { activeFilters.add("Seri: ${it.name}") }
                selectedModel?.let { activeFilters.add("Model: ${it.name}") }

                // Diğer filtreleri ekle
                filters.bodyType.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Kasa: $it") }
                filters.transmissionType.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Vites: $it") }
                filters.fuelType.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Yakıt: $it") }
                filters.driveType.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Çekiş: $it") }
                filters.color.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Renk: $it") }
                
                if (filters.priceRange.first > 0 || filters.priceRange.second < Int.MAX_VALUE) {
                    activeFilters.add("Fiyat: ${formatPrice(filters.priceRange.first)} - ${formatPrice(filters.priceRange.second)}")
                }
                if (filters.yearRange.first > 0 || filters.yearRange.second < 2024) {
                    activeFilters.add("Yıl: ${filters.yearRange.first} - ${filters.yearRange.second}")
                }
                if (filters.kilometersRange.first > 0 || filters.kilometersRange.second < Int.MAX_VALUE) {
                    activeFilters.add("KM: ${formatNumber(filters.kilometersRange.first)} - ${formatNumber(filters.kilometersRange.second)}")
                }
                if (filters.enginePowerRange.first > 0 || filters.enginePowerRange.second < 1000) {
                    activeFilters.add("Motor Gücü: ${filters.enginePowerRange.first} - ${filters.enginePowerRange.second} HP")
                }
                if (filters.engineVolumeRange.first > 0 || filters.engineVolumeRange.second < 10000) {
                    activeFilters.add("Motor Hacmi: ${filters.engineVolumeRange.first} - ${filters.engineVolumeRange.second} cc")
                }
                filters.heavyDamage?.let { if (it) activeFilters.add("Ağır Hasar") }
                
                filters.city.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Şehir: $it") }
                filters.district.takeIf { it.isNotEmpty() }?.let { activeFilters.add("İlçe: $it") }
                filters.neighborhood.takeIf { it.isNotEmpty() }?.let { activeFilters.add("Mahalle: $it") }

                // Filtreleri güncelle ve listeyi yenile
                _state.update { 
                    it.copy(
                        filters = filters,
                        activeFilters = activeFilters
                    )
                }
                
                currentPage = 1
                isInitialLoad = true
                loadListings()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Filtreler uygulanırken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSorting(sortBy: SortBy, sortOrder: SortOrder) {
        if (isLoadingListings) return

        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        filters = currentState.filters.copy(
                            sortBy = sortBy,
                            sortOrder = sortOrder
                        )
                    )
                }
                refreshListings()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Sıralama güncellenirken bir hata oluştu: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    @SuppressLint("DefaultLocale")
    private fun formatPrice(price: Int): String {
        return if (price == 0) "" else String.format("₺%,d", price).replace(",", ".")
    }

    @SuppressLint("DefaultLocale")
    private fun formatNumber(number: Int): String {
        return if (number == 0) "" else String.format("%,d", number).replace(",", ".")
    }

    fun refreshBrands() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingBrands = true, error = null) }
                when (val result = repository.getBrands()) {
                    is ResultState.Success -> {
                        _state.update { 
                            it.copy(
                                brands = result.data.brands.sortedBy { brand -> brand.name },
                                isLoadingBrands = false,
                                error = null
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoadingBrands = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoadingBrands = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Markalar yüklenirken bir hata oluştu: ${e.message}",
                        isLoadingBrands = false
                    )
                }
            }
        }
    }

    fun loadCities() {
        viewModelScope.launch {
            _cities.value = locationRepository.getCities().sorted()
        }
    }

    fun onCitySelected(city: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    filters = it.filters.copy(city = city, district = "", neighborhood = "")
                )
            }
            _districts.value = locationRepository.getDistricts(city)
            _neighborhoods.value = emptyList()
        }
    }

    fun onDistrictSelected(district: District) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    filters = it.filters.copy(district = district.name, neighborhood = "")
                )
            }
            _neighborhoods.value = locationRepository.getNeighborhoods(district.id)
        }
    }

    fun onNeighborhoodSelected(neighborhood: String) {
        _state.update {
            it.copy(
                filters = it.filters.copy(neighborhood = neighborhood)
            )
        }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        selectedBrand = null,
                        selectedSeries = null,
                        selectedModel = null,
                        filters = ListingFilters(
                            brandId = "",
                            seriesId = "",
                            modelId = "",
                            priceRange = Pair(0, 0),
                            yearRange = Pair(0, 2024),
                            fuelType = "",
                            transmissionType = "",
                            bodyType = "",
                            color = "",
                            kilometersRange = Pair(0, 0),
                            enginePowerRange = Pair(0, 0),
                            engineVolumeRange = Pair(0, 0),
                            driveType = "",
                            city = "",
                            district = "",
                            neighborhood = "",
                            heavyDamage = null
                        )
                    )
                }
                refreshListings()
            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error clearing filters: ${e.message}")
            }
        }
    }
} 