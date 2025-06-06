package com.carwise.android.viewmodel.update_listing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.*
import com.carwise.android.model.repository.CarwiseRepository
import com.carwise.android.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext

data class ImageUploadState(
    val uri: Uri,
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadedUrl: String? = null,
    val imageId: String? = null
)

@HiltViewModel
class UpdateListingViewModel @Inject constructor(
    private val repository: CarwiseRepository,
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(UpdateListingState())
    val state: StateFlow<UpdateListingState> = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts: StateFlow<List<District>> = _districts.asStateFlow()

    private val _neighborhoods = MutableStateFlow<List<String>>(emptyList())
    val neighborhoods: StateFlow<List<String>> = _neighborhoods.asStateFlow()

    private val _imageUploadStates = MutableStateFlow<List<ImageUploadState>>(emptyList())
    val imageUploadStates: StateFlow<List<ImageUploadState>> = _imageUploadStates.asStateFlow()

    fun loadInitialData(listingId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Load listing details
                when (val result = repository.getListingDetail(listingId)) {
                    is ResultState.Success -> {
                        val listing = result.data
                        
                        // Load brands data which includes all series and models
                        when (val brandsResult = repository.getBrands()) {
                            is ResultState.Success -> {
                                val brands = brandsResult.data.brands.sortedBy { brand -> brand.name }
                                
                                // Find the brand from brands list
                                val brand = brands.find { it.id == listing.brand.id }
                                
                                // Find the series from the brand's series list
                                val series = brand?.series?.find { it.id == listing.series.id }
                                
                                // Find the model from the series' models list
                                val model = series?.models?.find { it.id == listing.model.id }
                                
                                // Update state with all the data
                                _state.update { 
                                    it.copy(
                                        initialListing = listing,
                                        brands = brands,
                                        selectedBrand = brand,
                                        selectedSeries = series,
                                        selectedModel = model,
                                        selectedYear = listing.detail.year.toInt(),
                                        selectedBodyType = listing.detail.bodyType,
                                        selectedFuelType = listing.detail.fuelType,
                                        selectedTransmissionType = listing.detail.transmissionType,
                                        selectedDriveType = listing.detail.driveType,
                                        selectedColor = listing.detail.color,
                                        selectedHeavyDamage = listing.detail.heavyDamage,
                                        selectedCity = listing.city,
                                        selectedDistrict = null, // Will be set after loading districts
                                        selectedNeighborhood = listing.neighborhood,
                                        selectedPrice = listing.price,
                                        selectedCurrency = listing.currency,
                                        title = listing.title,
                                        description = listing.description,
                                        selectedEnginePower = listing.detail.enginePower,
                                        selectedEngineVolume = listing.detail.engineVolume,
                                        selectedKilometers = listing.detail.kilometers,
                                        frontBumper = listing.detail.frontBumper,
                                        frontHood = listing.detail.frontHood,
                                        roof = listing.detail.roof,
                                        frontRightDoor = listing.detail.frontRightDoor,
                                        rearRightDoor = listing.detail.rearRightDoor,
                                        frontLeftMudguard = listing.detail.frontLeftMudguard,
                                        frontLeftDoor = listing.detail.frontLeftDoor,
                                        rearLeftDoor = listing.detail.rearLeftDoor,
                                        rearLeftMudguard = listing.detail.rearLeftMudguard,
                                        rearBumper = listing.detail.rearBumper,
                                        isLoading = false
                                    )
                                }

                                // Load existing images
                                _imageUploadStates.value = listing.images.map { image ->
                                    ImageUploadState(
                                        uri = Uri.parse("https://carwisegw.yusuftalhaklc.com${image.path.removePrefix(".")}"),
                                        uploadedUrl = image.path,
                                        imageId = image.id
                                    )
                                }

                                // Load cities
                                _cities.value = locationRepository.getCities().sorted()
                                
                                // Load districts for selected city
                                if (listing.city.isNotEmpty()) {
                                    val districts = locationRepository.getDistricts(listing.city)
                                    _districts.value = districts.sortedBy { it.name }
                                    
                                    // Find and set the correct district
                                    val district = districts.find { it.name == listing.district }
                                    district?.let {
                                        _state.update { state ->
                                            state.copy(selectedDistrict = district)
                                        }
                                        
                                        // Load neighborhoods for selected district
                                        val neighborhoods = locationRepository.getNeighborhoods(district.id)
                                        _neighborhoods.value = neighborhoods.sorted()
                                    }
                                }
                            }
                            is ResultState.Error -> {
                                _state.update { 
                                    it.copy(
                                        error = brandsResult.error.error,
                                        isLoading = false
                                    )
                                }
                            }
                            is ResultState.Loading -> {
                                _state.update { it.copy(isLoading = true) }
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoading = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Veriler yüklenirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectBrand(brand: Brand) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it.copy(
                        selectedBrand = brand,
                        selectedSeries = null,
                        selectedModel = null,
                        isLoading = true,
                        error = null
                    )
                }

                // Get all brands data which includes series and models
                when (val result = repository.getBrands()) {
                    is ResultState.Success -> {
                        // Find the selected brand with its series and models
                        val updatedBrand = result.data.brands.find { it.id == brand.id }
                        updatedBrand?.let { selectedBrand ->
                            _state.update { currentState ->
                                currentState.copy(
                                    selectedBrand = selectedBrand,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoading = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Marka seçilirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectSeries(series: GetBrandSeries) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it.copy(
                        selectedSeries = series,
                        selectedModel = null,
                        isLoading = true,
                        error = null
                    )
                }

                // Get all brands data to find the selected series with its models
                when (val result = repository.getBrands()) {
                    is ResultState.Success -> {
                        // Find the selected brand and series
                        val selectedBrand = result.data.brands.find { it.id == _state.value.selectedBrand?.id }
                        val updatedSeries = selectedBrand?.series?.find { it.id == series.id }
                        
                        updatedSeries?.let { series ->
                            _state.update { currentState ->
                                currentState.copy(
                                    selectedSeries = series,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoading = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Seri seçilirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectModel(model: GetBrandSeriesModels) {
        _state.update { 
            it.copy(
                selectedModel = model,
                title = generateDefaultTitle()
            )
        }
    }

    fun selectYear(year: Int) {
        _state.update { it.copy(selectedYear = year) }
    }

    fun selectBodyType(bodyType: String) {
        _state.update { it.copy(selectedBodyType = bodyType) }
    }

    fun selectFuelType(fuelType: String) {
        _state.update { it.copy(selectedFuelType = fuelType) }
    }

    fun selectTransmissionType(transmissionType: String) {
        _state.update { it.copy(selectedTransmissionType = transmissionType) }
    }

    fun selectDriveType(driveType: String) {
        _state.update { it.copy(selectedDriveType = driveType) }
    }

    fun selectColor(color: String) {
        _state.update { it.copy(selectedColor = color) }
    }

    fun selectHeavyDamage(heavyDamage: Boolean) {
        _state.update { it.copy(selectedHeavyDamage = heavyDamage) }
    }

    fun updateDamageInfo(
        frontBumper: String? = null,
        frontHood: String? = null,
        roof: String? = null,
        frontRightDoor: String? = null,
        rearRightDoor: String? = null,
        frontLeftMudguard: String? = null,
        frontLeftDoor: String? = null,
        rearLeftDoor: String? = null,
        rearLeftMudguard: String? = null,
        rearBumper: String? = null
    ) {
        _state.update { currentState ->
            currentState.copy(
                frontBumper = frontBumper ?: currentState.frontBumper,
                frontHood = frontHood ?: currentState.frontHood,
                roof = roof ?: currentState.roof,
                frontRightDoor = frontRightDoor ?: currentState.frontRightDoor,
                rearRightDoor = rearRightDoor ?: currentState.rearRightDoor,
                frontLeftMudguard = frontLeftMudguard ?: currentState.frontLeftMudguard,
                frontLeftDoor = frontLeftDoor ?: currentState.frontLeftDoor,
                rearLeftDoor = rearLeftDoor ?: currentState.rearLeftDoor,
                rearLeftMudguard = rearLeftMudguard ?: currentState.rearLeftMudguard,
                rearBumper = rearBumper ?: currentState.rearBumper
            )
        }
    }

    fun selectEngineDetails(power: Long, volume: Long, kilometers: Long) {
        _state.update { 
            it.copy(
                selectedEnginePower = power,
                selectedEngineVolume = volume,
                selectedKilometers = kilometers
            )
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    private fun generateDefaultTitle(): String {
        val currentState = _state.value
        val year = currentState.selectedYear
        val brand = currentState.selectedBrand?.name
        val series = currentState.selectedSeries?.name
        val model = currentState.selectedModel?.name

        return buildString {
            if (year != null) append("$year ")
            if (brand != null) append("$brand ")
            if (series != null) append("$series ")
            if (model != null) append(model)
        }.trim()
    }

    fun formatPrice(price: String): Long? {
        return try {
            // Remove all non-digit characters
            val cleanPrice = price.replace(Regex("[^0-9]"), "")
            if (cleanPrice.isNotEmpty()) {
                cleanPrice.toLong()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun selectPrice(price: String) {
        val formattedPrice = formatPrice(price)
        _state.update { 
            it.copy(
                selectedPrice = formattedPrice,
                selectedCurrency = "TL"
            )
        }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun onCitySelected(city: String) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it.copy(
                        selectedCity = city,
                        selectedDistrict = null,
                        selectedNeighborhood = null
                    )
                }
                _districts.value = locationRepository.getDistricts(city)
                _neighborhoods.value = emptyList()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "İlçe bilgileri yüklenirken bir hata oluştu")
                }
            }
        }
    }

    fun onDistrictSelected(district: District) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it.copy(
                        selectedDistrict = district,
                        selectedNeighborhood = null,
                        isLoading = true,
                        error = null
                    )
                }

                // Load neighborhoods for selected district
                val neighborhoods = locationRepository.getNeighborhoods(district.id)
                _neighborhoods.value = neighborhoods.sorted()
                
                _state.update { 
                    it.copy(
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Mahalle bilgileri yüklenirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNeighborhoodSelected(neighborhood: String) {
        _state.update { it.copy(selectedNeighborhood = neighborhood) }
    }

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            try {
                // Maksimum fotoğraf sayısı kontrolü
                val currentUploadedCount = _imageUploadStates.value.count { it.uploadedUrl != null }
                if (currentUploadedCount + uris.size > 20) {
                    _state.update { it.copy(error = "En fazla 20 fotoğraf yükleyebilirsiniz") }
                    return@launch
                }

                // Yeni URI'leri ImageUploadState listesine ekle
                val newStates = uris.map { uri ->
                    // Dosya boyutu kontrolü (5MB)
                    val fileSize = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.available().toLong()
                    } ?: 0L

                    if (fileSize > 5 * 1024 * 1024) { // 5MB
                        ImageUploadState(uri, error = "Dosya boyutu 5MB'dan büyük olamaz")
                    } else {
                        ImageUploadState(uri, isLoading = true)
                    }
                }

                _imageUploadStates.update { currentStates ->
                    currentStates + newStates
                }

                // Her bir fotoğrafı yükle
                newStates.forEach { state ->
                    if (state.error == null) {
                        when (val result = uploadImage(state.uri)) {
                            is ResultState.Success -> {
                                _imageUploadStates.update { states ->
                                    states.map { currentState ->
                                        if (currentState.uri == state.uri) {
                                            currentState.copy(
                                                isLoading = false,
                                                uploadedUrl = result.data.image.path,
                                                imageId = result.data.image.id
                                            )
                                        } else currentState
                                    }
                                }
                            }
                            is ResultState.Error -> {
                                _imageUploadStates.update { states ->
                                    states.map { currentState ->
                                        if (currentState.uri == state.uri) {
                                            currentState.copy(
                                                isLoading = false,
                                                error = result.error.error
                                            )
                                        } else currentState
                                    }
                                }
                            }
                            is ResultState.Loading -> {
                                // Loading state is already set when we added the new states
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                _state.update { it.copy(error = "Fotoğraf yükleme hatası: ${e.message}") }
            }
        }
    }

    fun removeImage(index: Int) {
        viewModelScope.launch {
            try {
                _imageUploadStates.update { states ->
                    states.filterIndexed { i, _ -> i != index }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Fotoğraf silme hatası: ${e.message}") }
            }
        }
    }

    fun updateError(message: String) {
        _state.update { it.copy(error = message) }
    }

    private suspend fun uploadImage(uri: Uri): ResultState<ImageResponse> {
        return try {
            // Get file from URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", null, context.cacheDir)
            
            try {
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Check file size (5MB limit)
                if (tempFile.length() > 5 * 1024 * 1024) {
                    return ResultState.Error(ErrorResponse("Dosya boyutu 5MB'dan büyük olamaz"))
                }

                // Get file extension and validate
                val mimeType = context.contentResolver.getType(uri)
                val allowedMimeTypes = listOf(
                    "image/jpeg",
                    "image/jpg",
                    "image/png",
                    "image/gif"
                )

                if (mimeType == null || mimeType !in allowedMimeTypes) {
                    return ResultState.Error(
                        ErrorResponse("Sadece JPEG, JPG, PNG ve GIF formatında resimler yüklenebilir.")
                    )
                }

                // Create request body with proper content type
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(
                    "file",
                    "image.${mimeType.substringAfterLast('/')}",
                    requestBody
                )

                // Upload the file
                repository.upload(part)
            } finally {
                // Clean up temp file
                tempFile.delete()
            }
        } catch (e: Exception) {
            ResultState.Error(ErrorResponse("Fotoğraf yüklenirken bir hata oluştu: ${e.message}"))
        }
    }

    fun updateListing() {
        viewModelScope.launch {
            try {
                // Tüm fotoğrafların yüklenip yüklenmediğini kontrol et
                val pendingUploads = _imageUploadStates.value.any { it.isLoading }
                if (pendingUploads) {
                    _state.update { it.copy(error = "Lütfen fotoğrafların yüklenmesini bekleyin") }
                    return@launch
                }

                // Yüklenmemiş veya hatalı fotoğrafları kontrol et
                val failedUploads = _imageUploadStates.value.any { it.error != null }
                if (failedUploads) {
                    _state.update { it.copy(error = "Bazı fotoğraflar yüklenemedi. Lütfen kontrol edin.") }
                    return@launch
                }

                _state.update { it.copy(isLoading = true, error = null) }
                val currentState = state.value
                val initialListing = currentState.initialListing ?: return@launch

                val detail = Detail(
                    fuelType = currentState.selectedFuelType ?: "",
                    transmissionType = currentState.selectedTransmissionType ?: "",
                    bodyType = currentState.selectedBodyType ?: "",
                    driveType = currentState.selectedDriveType ?: "",
                    enginePower = currentState.selectedEnginePower ?: 0,
                    engineVolume = currentState.selectedEngineVolume ?: 0,
                    kilometers = currentState.selectedKilometers ?: 0,
                    year = currentState.selectedYear?.toLong() ?: 0,
                    color = currentState.selectedColor ?: "",
                    heavyDamage = currentState.selectedHeavyDamage ?: false,
                    frontBumper = currentState.frontBumper ?: "",
                    frontHood = currentState.frontHood ?: "",
                    roof = currentState.roof ?: "",
                    frontRightDoor = currentState.frontRightDoor ?: "",
                    rearRightDoor = currentState.rearRightDoor ?: "",
                    frontLeftMudguard = currentState.frontLeftMudguard ?: "",
                    frontLeftDoor = currentState.frontLeftDoor ?: "",
                    rearLeftDoor = currentState.rearLeftDoor ?: "",
                    rearLeftMudguard = currentState.rearLeftMudguard ?: "",
                    rearBumper = currentState.rearBumper ?: ""
                )

                // Resim ID'lerini al
                val imageIds = _imageUploadStates.value
                    .filter { it.imageId != null }
                    .map { it.imageId!! }

                val request = CreateListingRequest(
                    brandId = currentState.selectedBrand?.id ?: "",
                    seriesId = currentState.selectedSeries?.id ?: "",
                    modelId = currentState.selectedModel?.id ?: "",
                    title = currentState.title,
                    description = currentState.description,
                    currency = currentState.selectedCurrency,
                    price = currentState.selectedPrice ?: 0,
                    city = currentState.selectedCity ?: "",
                    district = currentState.selectedDistrict?.name ?: "",
                    neighborhood = currentState.selectedNeighborhood ?: "",
                    images = imageIds,
                    detail = detail
                )
                
                when (val result = repository.updateListing(initialListing.id, request)) {
                    is ResultState.Success -> {
                        _state.update { it.copy(isSuccess = true, isLoading = false) }
                        _navigationEvent.value = NavigationEvent.NavigateToListingDetail(initialListing.id)
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoading = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "İlan güncellenirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun goToPreviousStep() {
        val currentStep = _state.value.currentStep
        if (currentStep > 0) {
            _state.update { it.copy(currentStep = currentStep - 1) }
        }
    }

    fun goToNextStep() {
        val currentStep = _state.value.currentStep
        if (currentStep < 11) { // 11 is the last step (PreviewStep)
            _state.update { it.copy(currentStep = currentStep + 1) }
        }
    }

    fun updatePrice(price: Long?) {
        _state.update { 
            it.copy(
                selectedPrice = price,
                selectedCurrency = "TL"
            )
        }
    }
}

sealed class NavigationEvent {
    data class NavigateToListingDetail(val listingId: String) : NavigationEvent()
} 