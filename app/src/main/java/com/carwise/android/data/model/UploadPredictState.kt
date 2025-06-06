package com.carwise.android.data.model

import android.net.Uri

// PredictionResponse zaten mevcut olmalı

data class UploadPredictState(
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val prediction: PredictionResponse? = null,
    val error: String? = null
) 