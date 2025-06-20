package com.carwise.android.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.*
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: GetStatsResponse? = null,
    val users: List<UserResponse> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val totalUsers: Long = 0,
    val currentPage: Int = 1,
    val isPaginating: Boolean = false,
    val endReached: Boolean = false,
    val lastUpdated: Long = 0L,
    val isBrandLoading: Boolean = false,
    val isBrandOperationLoading: Boolean = false,
    val expandedBrands: Set<String> = emptySet(),
    val isContentVisible: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState = _dashboardState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadStats()
        loadUsers()
        loadBrands()
    }

    fun loadStats() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }
            try {
                when (val result = repository.getStats()) {
                    is ResultState.Success -> {
                        _dashboardState.update { 
                            it.copy(
                                isLoading = false,
                                stats = result.data,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isLoading = false,
                        error = "İstatistikler yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            if (_dashboardState.value.isPaginating) return@launch
            
            _dashboardState.update { it.copy(isPaginating = true) }
            try {
                when (val result = repository.getUsers(
                    page = _dashboardState.value.currentPage,
                    limit = 20
                )) {
                    is ResultState.Success -> {
                        _dashboardState.update { currentState ->
                            currentState.copy(
                                users = currentState.users + result.data.users,
                                totalUsers = result.data.total,
                                currentPage = currentState.currentPage + 1,
                                isPaginating = false,
                                endReached = currentState.users.size + result.data.users.size >= result.data.total,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isPaginating = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isPaginating = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isPaginating = false,
                        error = "Kullanıcılar yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadBrands() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandLoading = true) }
            try {
                when (val result = repository.getBrands()) {
                    is ResultState.Success -> {
                        _dashboardState.update { 
                            it.copy(
                                brands = result.data.brands,
                                isBrandLoading = false,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandLoading = false,
                        error = "Markalar yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun createBrand(name: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.createBrand(
                    brand = CreateBrandRequest(
                        name = name,
                        imageId = ""
                    )
                )) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Marka oluşturulurken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.deleteBrand(brandId)) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Marka silinirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun createSeries(brandId: String, name: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.createSeries(brandId, NameRequest(name))) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Seri oluşturulurken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteSeries(brandId: String, seriesId: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.deleteSeries(brandId, seriesId)) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Seri silinirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun createModel(brandId: String, seriesId: String, name: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.createModel(brandId, seriesId, NameRequest(name))) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Model oluşturulurken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteModel(brandId: String, seriesId: String, modelId: String) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isBrandOperationLoading = true) }
            try {
                when (val result = repository.deleteModel(brandId, seriesId, modelId)) {
                    is ResultState.Success -> {
                        loadBrands() // Refresh brands list
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(
                                isBrandOperationLoading = false,
                                error = result.error.error
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _dashboardState.update { it.copy(isBrandOperationLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(
                        isBrandOperationLoading = false,
                        error = "Model silinirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteUser(userId)) {
                    is ResultState.Success -> {
                        _dashboardState.update { currentState ->
                            currentState.copy(
                                users = currentState.users.filter { it.id != userId },
                                totalUsers = currentState.totalUsers - 1
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(error = result.error.error)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(error = "Kullanıcı silinirken bir hata oluştu: ${e.message}")
                }
            }
        }
    }

    fun updateUserRole(userId: String, newRole: Int) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateRole(userId, newRole)) {
                    is ResultState.Success -> {
                        // Update the user's role in the local state
                        _dashboardState.update { currentState ->
                            currentState.copy(
                                users = currentState.users.map { user ->
                                    if (user.id == userId) {
                                        user.copy(role = newRole.toLong())
                                    } else {
                                        user
                                    }
                                }
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _dashboardState.update { 
                            it.copy(error = result.error.error)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _dashboardState.update { 
                    it.copy(error = "Kullanıcı rolü güncellenirken bir hata oluştu: ${e.message}")
                }
            }
        }
    }

    fun refresh() {
        _dashboardState.update { 
            it.copy(
                currentPage = 1,
                users = emptyList(),
                brands = emptyList(),
                endReached = false,
                lastUpdated = 0L
            )
        }
        loadInitialData()
    }

    fun clearError() {
        _dashboardState.update { it.copy(error = null) }
    }

    fun toggleBrand(brandId: String) {
        _dashboardState.update { currentState ->
            currentState.copy(
                expandedBrands = if (currentState.expandedBrands.contains(brandId)) {
                    currentState.expandedBrands - brandId
                } else {
                    currentState.expandedBrands + brandId
                }
            )
        }
    }

    fun toggleAllBrands() {
        _dashboardState.update { currentState ->
            currentState.copy(
                expandedBrands = if (currentState.expandedBrands.size == currentState.brands.size) {
                    emptySet()
                } else {
                    currentState.brands.map { it.id }.toSet()
                }
            )
        }
    }

    fun toggleContentVisibility() {
        _dashboardState.update { currentState ->
            currentState.copy(
                isContentVisible = !currentState.isContentVisible,
                expandedBrands = emptySet()
            )
        }
    }
} 