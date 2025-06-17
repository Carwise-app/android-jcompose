package com.carwise.android.viewmodel.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UpdateListingStatusRequest
import com.carwise.android.data.model.UserPayload
import com.carwise.android.data.model.PricePredictionRequest
import com.carwise.android.data.model.PricePredictionResponse
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.carwise.android.data.model.PredictionResponse
import kotlinx.coroutines.flow.update

data class ListingDetailState(
    val listing: GetListingResponse? = null,
    val currentUser: UserPayload? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val error: String? = null,
    // Prediction state
    val isPredicting: Boolean = false,
    val prediction: PricePredictionResponse? = null,
    val predictionError: String? = null,
    val imagePredictions: Map<String, PredictionResponse> = emptyMap()
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ListingDetailState())
    val state: StateFlow<ListingDetailState> = _state


    init {
        getCurrentUser()
    }

    fun loadListingDetail(listingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val result = repository.getListingDetail(listingId)
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            listing = result.data
                        )
                        predictAllImages(result.data.images)
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "İlan detayı yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun toggleFavorite() {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isFavoriteLoading = true)
            try {
                val result = if (_state.value.listing!!.isFavorite) {
                    repository.deleteFavorite(listingId)
                } else {
                    repository.createFavorite(listingId)
                }
                
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            listing = _state.value.listing?.copy(isFavorite = !_state.value.listing!!.isFavorite),
                            isFavoriteLoading = false
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isFavoriteLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isFavoriteLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isFavoriteLoading = false,
                    error = "Favori işlemi sırasında bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun getCurrentUser(){
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                _state.value = _state.value.copy(
                    currentUser = currentUser
                )
            }
        }
    }

    fun updateListingStatus(status: Int) {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            try {
                val result = repository.updateStatusListing(
                    listingId = listingId,
                    request = UpdateListingStatusRequest(status = status)
                )
                
                when (result) {
                    is ResultState.Success -> {
                        loadListingDetail(listingId)
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isActionLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isActionLoading = false,
                    error = "İlan durumu güncellenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun deleteListing() {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            try {
                val result = repository.deleteListing(listingId)
                
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            listing = null,
                            error = null
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isActionLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isActionLoading = false,
                    error = "İlan silinirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun predictPrice() {
        viewModelScope.launch {
            val listing = state.value.listing ?: return@launch
            val detail = listing.detail

            // Reset prediction state
            _state.value = _state.value.copy(
                isPredicting = true,
                prediction = null,
                predictionError = null
            )

            val parts = listOf(
                detail.frontBumper, detail.frontHood, detail.roof,
                detail.frontRightDoor, detail.rearRightDoor,
                detail.frontLeftMudguard, detail.frontLeftDoor,
                detail.rearLeftDoor, detail.rearLeftMudguard,
                detail.rearBumper
            )

            val boyalSayisi = parts.count { it.lowercase() in listOf("boyalı", "lokal", "lokal boya") }
            val degisenSayisi = parts.count { it.lowercase() == "değişmiş" }
            val orjinalSayisi = parts.count { it.lowercase() in listOf("orijinal", "yok") }

            val request = PricePredictionRequest(
                marka = listing.brand.name,
                seri = listing.series.name,
                model = listing.model.name,
                yl = detail.year.toLong(),
                kilometre = detail.kilometers.toDouble(),
                motorHacmi = detail.engineVolume.toDouble(),
                motorGucu = detail.enginePower.toDouble(),
                tramer = if (detail.heavyDamage) 1.0 else 0.0,
                boyalSayisi = boyalSayisi.toLong(),
                degisenSayisi = degisenSayisi.toLong(),
                orjinalSayisi = orjinalSayisi.toLong(),
                vitesTipi = if (detail.transmissionType == "Manuel") "Düz" else detail.transmissionType,
                yakitTipi = detail.fuelType,
                kasaTipi = detail.bodyType,
                renk = detail.color
            )

            delay(2000)
            
            val result = repository.pricePredict(request)
            when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            isPredicting = false,
                            prediction = result.data,
                            predictionError = null
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isPredicting = false,
                            prediction = null,
                            predictionError = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isPredicting = true)
                    }
            }

        }
    }

    fun resetPrediction() {
        _state.value = _state.value.copy(
            isPredicting = false,
            prediction = null,
            predictionError = null
        )
    }

    fun predictAllImages(images: List<com.carwise.android.data.model.Image>) {
        viewModelScope.launch {
            images.forEach { image ->
                val imageId = image.id ?: return@forEach
                val result = repository.uploadPredict(imageId)
                when(result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            imagePredictions = _state.value.imagePredictions + (imageId to result.data)
                        )
                    }

                    is ResultState.Error -> {
                        // Handle error case - you might want to log or show error
                        // For example:
                        // Log.e("PredictImages", "Failed to predict image $imageId: ${result.message}")
                    }

                    ResultState.Loading -> {
                        // This case might not be needed here since you're handling async calls
                        // The loading state would typically be managed at a higher level
                    }
                }
            }
        }
    }
} 