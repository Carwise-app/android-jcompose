package com.carwise.android.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.ProfileResponse
import com.carwise.android.data.model.ResultState
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val profile: ProfileResponse? = null,
    val myListingsCount: Int = 0,
    val favoritesCount: Int = 0,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateError: String? = null,
    val isUpdateSuccess: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleteSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            if (_profileState.value.isLoading) return@launch
            
            _profileState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load profile details
                when (val result = repository.getProfileDetail()) {
                    is ResultState.Success -> {
                        val profileResponse = result.data
                        _profileState.update { it.copy(profile = profileResponse) }

                        // Load listings count in parallel with favorites
                        val listingsJob = launch {
                            when (val listingsResult = repository.listListing(
                                createdBy = profileResponse.id,
                                page = 1,
                                limit = 1
                            )) {
                                is ResultState.Success -> {
                                    _profileState.update { it.copy(myListingsCount = listingsResult.data.total) }
                                }
                                is ResultState.Error -> {
                                    _profileState.update { 
                                        it.copy(error = "İlan sayısı alınamadı: ${listingsResult.error.error}")
                                    }
                                }
                                is ResultState.Loading -> {
                                    // Loading state is handled by isLoading flag
                                }
                            }
                        }

                        // Load favorites count in parallel
                        val favoritesJob = launch {
                            when (val favoritesResult = repository.getFavorites(page = 1, limit = 1)) {
                                is ResultState.Success -> {
                                    _profileState.update { it.copy(favoritesCount = favoritesResult.data.total) }
                                }
                                is ResultState.Error -> {
                                    _profileState.update { 
                                        it.copy(error = "Favori sayısı alınamadı: ${favoritesResult.error.error}")
                                    }
                                }
                                is ResultState.Loading -> {
                                    // Loading state is handled by isLoading flag
                                }
                            }
                        }

                        // Wait for both jobs to complete
                        listingsJob.join()
                        favoritesJob.join()
                    }
                    is ResultState.Error -> {
                        _profileState.update { 
                            it.copy(error = "Profil bilgileri alınamadı: ${result.error.error}")
                        }
                    }
                    is ResultState.Loading -> {
                        // Loading state is handled by isLoading flag
                    }
                }
            } catch (e: Exception) {
                _profileState.update { 
                    it.copy(error = "Profil bilgileri yüklenirken bir hata oluştu: ${e.message}")
                }
            } finally {
                _profileState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, countryCode: String, phoneNumber: String) {
        if (_profileState.value.isUpdating) return

        // Input validation
        when {
            firstName.isBlank() -> {
                _profileState.update { it.copy(updateError = "Ad alanı boş bırakılamaz.") }
                return
            }
            lastName.isBlank() -> {
                _profileState.update { it.copy(updateError = "Soyad alanı boş bırakılamaz.") }
                return
            }
            countryCode.isBlank() -> {
                _profileState.update { it.copy(updateError = "Ülke kodu boş bırakılamaz.") }
                return
            }
            phoneNumber.isBlank() -> {
                _profileState.update { it.copy(updateError = "Telefon alanı boş bırakılamaz.") }
                return
            }
            !phoneNumber.matches(Regex("^[0-9]{10}$")) -> {
                _profileState.update { it.copy(updateError = "Geçerli bir telefon numarası giriniz (10 haneli).") }
                return
            }
        }

        viewModelScope.launch {
            _profileState.update { 
                it.copy(isUpdating = true, updateError = null, isUpdateSuccess = false)
            }

            try {
                when (val result = repository.updateProfile(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    countryCode = countryCode.trim(),
                    phoneNumber = phoneNumber.trim()
                )) {
                    is ResultState.Success -> {
                        _profileState.update { 
                            it.copy(
                                isUpdating = false,
                                profile = result.data,
                                isUpdateSuccess = true
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _profileState.update { 
                            it.copy(
                                isUpdating = false,
                                updateError = result.error.error,
                                isUpdateSuccess = false
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        // Loading state is handled by isUpdating flag
                    }
                }
            } catch (e: Exception) {
                _profileState.update { 
                    it.copy(
                        isUpdating = false,
                        updateError = "Profil güncellenirken bir hata oluştu: ${e.message}",
                        isUpdateSuccess = false
                    )
                }
            }
        }
    }

    fun refresh() {
        if (!_profileState.value.isLoading) {
            loadProfileData()
        }
    }

    fun clearErrors() {
        _profileState.update { 
            it.copy(error = null, updateError = null)
        }
    }

    fun clearUpdateState() {
        _profileState.update { 
            it.copy(
                isUpdateSuccess = false,
                updateError = null,
                isUpdating = false
            )
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _profileState.update { it.copy(isDeleting = true, error = null) }
            try {
                when (val result = repository.deleteUser(
                    id = _profileState.value.profile?.id ?: ""
                )) {
                    is ResultState.Success -> {
                        _profileState.update { it.copy(isDeleteSuccess = true) }
                    }
                    is ResultState.Error -> {
                        _profileState.update { it.copy(error = result.error.error) }
                    }

                    ResultState.Loading -> {}
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(error = e.message ?: "Bir hata oluştu") }
            } finally {
                _profileState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun clearDeleteState() {
        _profileState.update { it.copy(isDeleteSuccess = false) }
    }
} 