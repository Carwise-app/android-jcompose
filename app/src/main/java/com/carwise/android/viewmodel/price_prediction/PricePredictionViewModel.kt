package com.carwise.android.viewmodel.price_prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.Brand
import com.carwise.android.data.model.GetBrandSeries
import com.carwise.android.data.model.GetBrandSeriesModels
import com.carwise.android.data.model.PricePredictionRequest
import com.carwise.android.data.model.PricePredictionResponse
import com.carwise.android.data.model.ResultState
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    object NavigateToResult : NavigationEvent()
    object NavigateToHistory : NavigationEvent()
}

data class PricePredictionState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val predictedPrice: Long? = null,
    
    // Selection states
    val brands: List<Brand> = emptyList(),
    val series: List<GetBrandSeries> = emptyList(),
    val models: List<GetBrandSeriesModels> = emptyList(),
    val selectedBrand: Brand? = null,
    val selectedSeries: GetBrandSeries? = null,
    val selectedModel: GetBrandSeriesModels? = null,
    val selectedYear: Int? = null,
    val selectedKilometers: Long? = null,
    val selectedTransmissionType: String? = null,
    val selectedFuelType: String? = null,
    val selectedBodyType: String? = null,
    val selectedColor: String? = null,
    val selectedEngineVolume: Long? = null,
    val selectedEnginePower: Long? = null,
    val selectedTramerAmount: Long? = null,
    
    // Damage states
    val frontBumper: String? = null,
    val frontHood: String? = null,
    val roof: String? = null,
    val frontRightDoor: String? = null,
    val rearRightDoor: String? = null,
    val frontLeftMudguard: String? = null,
    val frontLeftDoor: String? = null,
    val rearLeftDoor: String? = null,
    val rearLeftMudguard: String? = null,
    val rearBumper: String? = null,
    val predictionResult: PricePredictionResponse? = null,
    val canPredict: Boolean = false
)

@HiltViewModel
class PricePredictionViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PricePredictionState())
    val state: StateFlow<PricePredictionState> = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        loadBrands()
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getBrands()) {
                is ResultState.Success -> {
                    _state.update { 
                        it.copy(
                            brands = result.data.brands.sortedBy { brand -> brand.name },
                            isLoading = false,
                            error = null
                        )
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
        }
    }

    private fun loadSeries(brandId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val brand = _state.value.brands.find { it.id == brandId }
                brand?.let {
                    _state.update { state -> 
                        state.copy(
                            series = it.series
                                .distinctBy { series -> series.name }
                                .sortedBy { series -> series.name },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Seriler yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    private fun loadModels(seriesId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val series = _state.value.series.find { it.id == seriesId }
                series?.let {
                    _state.update { state -> 
                        state.copy(
                            models = it.models
                                .distinctBy { model -> model.name }
                                .sortedBy { model -> model.name },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Modeller yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun selectBrand(brand: Brand) {
        _state.update { 
            it.copy(
                selectedBrand = brand,
                canPredict = canPredictPrice()
            )
        }
        loadSeries(brand.id)
    }

    fun selectSeries(series: GetBrandSeries) {
        _state.update { 
            it.copy(
                selectedSeries = series,
                canPredict = canPredictPrice()
            )
        }
        loadModels(series.id)
    }

    fun selectModel(model: GetBrandSeriesModels) {
        _state.update { 
            it.copy(
                selectedModel = model,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectYear(year: Int) {
        _state.update { 
            it.copy(
                selectedYear = year,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectKilometers(kilometers: Long) {
        _state.update { 
            it.copy(
                selectedKilometers = kilometers,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectTransmissionType(type: String) {
        _state.update { 
            it.copy(
                selectedTransmissionType = type,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectFuelType(type: String) {
        _state.update { 
            it.copy(
                selectedFuelType = type,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectBodyType(type: String) {
        _state.update { 
            it.copy(
                selectedBodyType = type,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectColor(color: String) {
        _state.update { 
            it.copy(
                selectedColor = color,
                canPredict = canPredictPrice()
            )
        }
    }

    fun selectEngineDetails(power: Long, volume: Long) {
        _state.update { 
            it.copy(
                selectedEnginePower = power,
                selectedEngineVolume = volume,
                canPredict = canPredictPrice()
            )
        }
    }

    private fun canPredictPrice(): Boolean {
        val state = _state.value
        return state.selectedBrand != null &&
               state.selectedSeries != null &&
               state.selectedModel != null &&
               state.selectedYear != null &&
               state.selectedKilometers != null &&
               state.selectedTransmissionType != null &&
               state.selectedFuelType != null &&
               state.selectedBodyType != null &&
               state.selectedColor != null &&
               state.selectedEnginePower != null &&
               state.selectedEngineVolume != null &&
               state.selectedTramerAmount != null
    }

    fun selectTramerAmount(amount: Long) {
        _state.update { 
            it.copy(
                selectedTramerAmount = amount,
                canPredict = canPredictPrice()
            )
        }
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

    fun goToNextStep() {
        if (_state.value.currentStep < 11) { // 11 steps total
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        } else {
            predictPrice()
        }
    }

    fun goToPreviousStep() {
        if (_state.value.currentStep > 0) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun predictPrice() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val request = PricePredictionRequest(
                    marka = state.value.selectedBrand?.name ?: "",
                    seri = state.value.selectedSeries?.name ?: "",
                    model = state.value.selectedModel?.name ?: "",
                    yl = state.value.selectedYear?.toLong() ?: 0L,
                    kilometre = state.value.selectedKilometers?.toDouble() ?: 0.0,
                    motorHacmi = state.value.selectedEngineVolume?.toDouble() ?: 0.0,
                    motorGucu = state.value.selectedEnginePower?.toDouble() ?: 0.0,
                    tramer = state.value.selectedTramerAmount?.toDouble() ?: 0.0,
                    boyalSayisi = calculatePaintedParts(),
                    degisenSayisi = calculateReplacedParts(),
                    orjinalSayisi = calculateOriginalParts(),
                    vitesTipi = when(state.value.selectedTransmissionType) {
                        "Manuel" -> "Düz"
                        else -> state.value.selectedTransmissionType ?: ""
                    },
                    yakitTipi = state.value.selectedFuelType ?: "",
                    kasaTipi = when(state.value.selectedBodyType) {
                    "Hatchback " -> "Hatchback/5"
                    else -> state.value.selectedBodyType ?: ""
                    },
                        renk = state.value.selectedColor ?: ""
                    )

                when (val result = repository.pricePredict(request)) {
                    is ResultState.Success -> {
                        _state.update { 
                            it.copy(
                                predictionResult = result.data,
                                isLoading = false
                            )
                        }
                        _navigationEvent.value = NavigationEvent.NavigateToResult
                    }
                    is ResultState.Error -> {
                        _state.update { 
                            it.copy(
                                error = result.error.error,
                                isLoading = false
                            )
                        }
                    }

                    ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Tahmin yapılırken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculatePaintedParts(): Long {
        var count = 0L
        if (state.value.frontBumper == "Boyalı") count++
        if (state.value.frontHood == "Boyalı") count++
        if (state.value.roof == "Boyalı") count++
        if (state.value.frontRightDoor == "Boyalı") count++
        if (state.value.rearRightDoor == "Boyalı") count++
        if (state.value.frontLeftMudguard == "Boyalı") count++
        if (state.value.frontLeftDoor == "Boyalı") count++
        if (state.value.rearLeftDoor == "Boyalı") count++
        if (state.value.rearLeftMudguard == "Boyalı") count++
        if (state.value.rearBumper == "Boyalı") count++
        return count
    }

    private fun calculateReplacedParts(): Long {
        var count = 0L
        if (state.value.frontBumper == "Değişmiş") count++
        if (state.value.frontHood == "Değişmiş") count++
        if (state.value.roof == "Değişmiş") count++
        if (state.value.frontRightDoor == "Değişmiş") count++
        if (state.value.rearRightDoor == "Değişmiş") count++
        if (state.value.frontLeftMudguard == "Değişmiş") count++
        if (state.value.frontLeftDoor == "Değişmiş") count++
        if (state.value.rearLeftDoor == "Değişmiş") count++
        if (state.value.rearLeftMudguard == "Değişmiş") count++
        if (state.value.rearBumper == "Değişmiş") count++
        return count
    }

    private fun calculateOriginalParts(): Long {
        var count = 0L
        if (state.value.frontBumper == "Orijinal") count++
        if (state.value.frontHood == "Orijinal") count++
        if (state.value.roof == "Orijinal") count++
        if (state.value.frontRightDoor == "Orijinal") count++
        if (state.value.rearRightDoor == "Orijinal") count++
        if (state.value.frontLeftMudguard == "Orijinal") count++
        if (state.value.frontLeftDoor == "Orijinal") count++
        if (state.value.rearLeftDoor == "Orijinal") count++
        if (state.value.rearLeftMudguard == "Orijinal") count++
        if (state.value.rearBumper == "Orijinal") count++
        return count
    }

    fun navigateToHistory() {
        viewModelScope.launch {
            _navigationEvent.value = NavigationEvent.NavigateToHistory
        }
    }

    fun resetPrediction() {
        _state.update { 
            it.copy(
                predictionResult = null,
                currentStep = 0,
                selectedBrand = null,
                selectedSeries = null,
                selectedModel = null,
                selectedYear = null,
                selectedKilometers = null,
                selectedTransmissionType = null,
                selectedFuelType = null,
                selectedBodyType = null,
                selectedColor = null,
                selectedEnginePower = null,
                selectedEngineVolume = null,
                selectedTramerAmount = null,
                frontBumper = null,
                frontHood = null,
                roof = null,
                frontRightDoor = null,
                rearRightDoor = null,
                frontLeftMudguard = null,
                frontLeftDoor = null,
                rearLeftDoor = null,
                rearLeftMudguard = null,
                rearBumper = null,
                error = null
            )
        }
    }
} 