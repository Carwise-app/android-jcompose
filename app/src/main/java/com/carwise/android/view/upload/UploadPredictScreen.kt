package com.carwise.android.view.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.upload.UploadPredictViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPredictScreen(
    navController: NavController,
    viewModel: UploadPredictViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.setImageUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload & Predict", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = appRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.selectedImageUri != null) {
                AsyncImage(
                    model = state.selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(16.dp))
            }
            Button(
                onClick = { launcher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appRed)
            ) {
                Text(if (state.selectedImageUri == null) "Resim Seç" else "Resmi Değiştir", color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.uploadAndPredict(context) },
                enabled = state.selectedImageUri != null && !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appRed)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Yükle ve Tahmin Et", color = Color.White)
                }
            }
            Spacer(Modifier.height(32.dp))
            if (state.prediction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tahmin Sonucu", style = MaterialTheme.typography.titleMedium, color = appRed)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (state.prediction!!.prediction.prediction) "ARABA" else "ARABA DEĞİL",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        state.prediction!!.prediction.confidence.let { confidence ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Güven: %${(confidence * 100).toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            if (state.error != null) {
                Text(state.error.toString(), color = Color.Red)
            }
        }
    }
} 