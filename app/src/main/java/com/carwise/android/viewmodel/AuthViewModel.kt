package com.carwise.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.ForgotPasswordRequest
import com.carwise.android.data.model.LoginRequest
import com.carwise.android.data.model.RegisterRequest
import com.carwise.android.data.model.ResetPasswordQuery
import com.carwise.android.data.model.ResetPasswordRequest
import com.carwise.android.data.model.ResultState
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isSendingOtp: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: CarwiseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val isAuthenticated = repository.isLoggedIn()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = isAuthenticated
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        countryCode: String,
        phoneNumber: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val result = repository.register(
                    request = RegisterRequest(
                        firstName = firstName.trimStart().trimEnd(),
                        lastName = lastName.trimStart().trimEnd(),
                        countryCode = countryCode,
                        phoneNumber = phoneNumber.trim().replace(" ",""),
                        email = email.lowercase().trimStart().trimEnd(),
                        password = password.trimStart().trimEnd()
                    )
                )
                when (result) {
                    is ResultState.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    is ResultState.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                        kotlinx.coroutines.delay(1000)
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is ResultState.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Bilinmeyen bir hata oluştu"
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val result = repository.login(
                    request = LoginRequest(
                        email = email.lowercase().trimStart().trimEnd(),
                        password = password.trimStart().trimEnd()
                    )
                )
                when (result) {
                    is ResultState.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    is ResultState.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                        kotlinx.coroutines.delay(1000)  
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is ResultState.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Bilinmeyen bir hata oluştu"
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val result = repository.loginWithGoogle(
                   idToken = idToken
                )
                when (result) {
                    is ResultState.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    is ResultState.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                        kotlinx.coroutines.delay(1000)
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is ResultState.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                repository.logout()
                kotlinx.coroutines.delay(1000)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = false
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val result = repository.forgotPassword(
                    request = ForgotPasswordRequest(
                        email = email.lowercase().trimStart().trimEnd(),
                    )
                )
                when (result) {
                    is ResultState.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isSendingOtp = true
                        )
                    }
                    is ResultState.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                        kotlinx.coroutines.delay(1000)
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is ResultState.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Bilinmeyen bir hata oluştu"
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun resetPassword(email: String, token: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            try {
                val result = repository.resetPassword(
                    query = ResetPasswordQuery(
                        email = email.lowercase().trimStart().trimEnd(),
                        token = token.trimStart().trimEnd()
                    ),
                    request = ResetPasswordRequest(
                        password = newPassword.trimStart().trimEnd(),
                        rePassword = newPassword.trimStart().trimEnd()
                    )
                )
                when (result) {
                    is ResultState.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    is ResultState.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                        kotlinx.coroutines.delay(1000)
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    is ResultState.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Bilinmeyen bir hata oluştu"
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
} 