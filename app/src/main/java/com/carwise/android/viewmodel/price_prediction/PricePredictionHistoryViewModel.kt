package com.carwise.android.viewmodel.price_prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.PricePredict
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.GetPricePredictHistoryResponse
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PricePredictionHistoryState(
    val predictions: List<PricePredict> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true
)

@HiltViewModel
class PricePredictionHistoryViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PricePredictionHistoryState())
    val state: StateFlow<PricePredictionHistoryState> = _state.asStateFlow()

    init {
        loadPredictionHistory()
    }

    private fun loadPredictionHistory(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = repository.getPredictionHistory(page = page)) {
                    is ResultState.Success -> {
                        val response = result.data
                        _state.update { currentState -> 
                            currentState.copy(
                                predictions = if (page == 1) response.predicts else currentState.predictions + response.predicts,
                                isLoading = false,
                                currentPage = page,
                                hasMorePages = response.predicts.isNotEmpty()
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
                    ResultState.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Tahmin geçmişi yüklenirken bir hata oluştu: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadMorePredictions() {
        if (!state.value.isLoading && state.value.hasMorePages) {
            loadPredictionHistory(state.value.currentPage + 1)
        }
    }

    fun refresh() {
        loadPredictionHistory(1)
    }
} 