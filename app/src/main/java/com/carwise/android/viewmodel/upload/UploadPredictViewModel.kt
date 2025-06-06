package com.carwise.android.viewmodel.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.model.repository.CarwiseRepository
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UploadPredictState
import com.carwise.android.data.model.PredictionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadPredictViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UploadPredictState())
    val state: StateFlow<UploadPredictState> = _state.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _state.update { it.copy(selectedImageUri = uri, error = null, prediction = null) }
    }

    fun uploadAndPredict(context: Context) {
        val uri = state.value.selectedImageUri ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, prediction = null) }
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Resim okunamadı")
                val file = File.createTempFile("upload_image", ".jpg", context.cacheDir)
                inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val uploadResult = repository.upload(body)
                val fileId = when (uploadResult) {
                    is ResultState.Success -> uploadResult.data.image.id
                    is ResultState.Error -> {
                        _state.update { it.copy(isLoading = false, error = uploadResult.error.error) }
                        null
                    }
                    is ResultState.Loading -> null
                } ?: throw Exception("Yükleme başarısız veya id alınamadı.")

                val predictResult = repository.uploadPredict(fileId)
                when (predictResult) {
                    is ResultState.Success -> {
                        _state.update { it.copy(isLoading = false, prediction = predictResult.data, error = null) }
                    }
                    is ResultState.Error -> {
                        _state.update { it.copy(isLoading = false, error = predictResult.error.error) }
                    }
                    is ResultState.Loading -> {}
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Bilinmeyen bir hata oluştu") }
            }
        }
    }
} 