package com.carwise.android.view.price_prediction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.price_prediction.PricePredictionViewModel
import com.carwise.android.viewmodel.price_prediction.PricePredictionState
import com.carwise.android.viewmodel.price_prediction.NavigationEvent
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.carwise.android.data.model.PricePredictionResponse
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricePredictionScreen(
    navController: NavController,
    viewModel: PricePredictionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    var showResult by remember { mutableStateOf(false) }
    var resultAnimation by remember { mutableStateOf(0f) }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToResult -> {
                viewModel.onNavigationHandled()
                showResult = true
                // Animasyonu başlat
                animate(0f, 1f, animationSpec = tween(1000)) { value, _ ->
                    resultAnimation = value
                }
            }
            is NavigationEvent.NavigateToHistory -> {
                viewModel.onNavigationHandled()
                navController.navigate(Screen.PricePredictionHistory.route) {
                    popUpTo(Screen.PricePrediction.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            null -> {}
        }
    }

    val steps = listOf(
        "Marka",
        "Seri",
        "Model",
        "Yıl",
        "Kilometre",
        "Vites",
        "Yakıt",
        "Kasa",
        "Renk",
        "Hasar",
        "Motor",
        "Tramer"
    )

    val stepIcons = listOf(
        Icons.Default.DirectionsCar,
        Icons.Default.DirectionsCar,
        Icons.Default.DirectionsCar,
        Icons.Default.DateRange,
        Icons.Default.Speed,
        Icons.Default.Settings,
        Icons.Default.LocalGasStation,
        Icons.Default.Build,
        Icons.Default.ColorLens,
        Icons.Default.Build,
        Icons.Default.Build,
        Icons.Default.AttachMoney
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiyat Tahmini", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = appRed)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateToHistory() }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Tahmin Geçmişi",
                            tint = appRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            } else if (showResult) {
                state.predictionResult?.let { result ->
                    PriceResultCard(
                        result = result,
                        onNewPrediction = {
                            showResult = false
                            viewModel.resetPrediction()
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stepper
                    StepIndicator(
                        currentStep = state.currentStep,
                        steps = steps,
                        stepIcons = stepIcons,
                        tint = appRed
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Step Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (state.currentStep) {
                            0 -> BrandSelectionStep(state, viewModel)
                            1 -> SeriesSelectionStep(state, viewModel)
                            2 -> ModelSelectionStep(state, viewModel)
                            3 -> YearSelectionStep(state, viewModel)
                            4 -> KilometersSelectionStep(state, viewModel)
                            5 -> TransmissionTypeSelectionStep(state, viewModel)
                            6 -> FuelTypeSelectionStep(state, viewModel)
                            7 -> BodyTypeSelectionStep(state, viewModel)
                            8 -> ColorSelectionStep(state, viewModel)
                            9 -> DamageSelectionStep(state, viewModel)
                            10 -> EngineDetailsStep(state, viewModel)
                            11 -> TramerAmountStep(state, viewModel)
                        }
                    }

                    // Navigation Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button
                        if (state.currentStep > 0) {
                            OutlinedButton(
                                onClick = { viewModel.goToPreviousStep() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = appRed
                                ),
                                border = BorderStroke(1.dp, appRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Geri")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Next/Predict Button
                        if (state.currentStep < steps.size - 1) {
                            val isNextEnabled = when (state.currentStep) {
                                0 -> state.selectedBrand != null
                                1 -> state.selectedSeries != null
                                2 -> state.selectedModel != null
                                3 -> state.selectedYear != null
                                4 -> state.selectedKilometers != null
                                5 -> state.selectedTransmissionType != null
                                6 -> state.selectedFuelType != null
                                7 -> state.selectedBodyType != null
                                8 -> state.selectedColor != null
                                9 -> true // Damage step is optional
                                10 -> state.selectedEnginePower != null && state.selectedEngineVolume != null
                                11 -> state.selectedTramerAmount != null
                                else -> true
                            }

                            Button(
                                onClick = { viewModel.goToNextStep() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = appRed),
                                shape = RoundedCornerShape(12.dp),
                                enabled = isNextEnabled
                            ) {
                                Text("İleri")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Son adımda Tahmin Et butonu göster
                            Button(
                                onClick = { viewModel.predictPrice() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = appRed),
                                shape = RoundedCornerShape(12.dp),
                                enabled = state.canPredict
                            ) {
                                Text("Tahmin Et")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Error Message
                    state.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Marka Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.brands) { brand ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectBrand(brand) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (brand == state.selectedBrand) 
                                appRed.copy(alpha = 0.1f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = brand.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (brand == state.selectedBrand) appRed else Color.Black
                            )
                            if (brand == state.selectedBrand) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = appRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seri Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        state.selectedBrand?.let { brand ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.series) { series ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSeries(series) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (series == state.selectedSeries) 
                                    appRed.copy(alpha = 0.1f) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                series.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (series == state.selectedSeries) appRed else Color.Black
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Önce bir marka seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ModelSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Model Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        state.selectedSeries?.let { series ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.models) { model ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectModel(model) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (model == state.selectedModel) 
                                    appRed.copy(alpha = 0.1f) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                model.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (model == state.selectedModel) appRed else Color.Black
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Önce bir seri seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun YearSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Yıl Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val years = (2024 downTo 1950).toList()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(years) { year ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectYear(year) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (year == state.selectedYear) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        year.toString(),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (year == state.selectedYear) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun KilometersSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    var kilometers by remember { mutableStateOf(state.selectedKilometers?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Kilometre Bilgisi",
            style = MaterialTheme.typography.titleLarge,
            color = appRed
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = kilometers,
            onValueChange = { 
                kilometers = it.filter { char -> char.isDigit() }
                if (kilometers.isNotEmpty()) {
                    viewModel.selectKilometers(kilometers.toLong())
                }
            },
            label = { Text("Kilometre") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appRed,
                focusedLabelColor = appRed
            ),
            prefix = { Text("KM ") }
        )
    }
}

@Composable
private fun TransmissionTypeSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Vites Tipi Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val transmissionTypes = listOf("Manuel", "Otomatik", "Yarı Otomatik")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transmissionTypes) { type ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTransmissionType(type) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (type == state.selectedTransmissionType) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        type,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (type == state.selectedTransmissionType) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun FuelTypeSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Yakıt Tipi Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val fuelTypes = listOf("Benzin", "Dizel", "LPG", "Hibrit", "Elektrik")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fuelTypes) { type ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectFuelType(type) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (type == state.selectedFuelType) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        type,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (type == state.selectedFuelType) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyTypeSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Kasa Tipi Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val bodyTypes = listOf("Sedan", "Hatchback", "SUV", "Station Wagon", "Coupe", "Cabrio", "Pickup")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bodyTypes) { type ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectBodyType(type) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (type == state.selectedBodyType) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        type,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (type == state.selectedBodyType) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Renk Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val colors = listOf(
            "Siyah", "Beyaz", "Gri", "Kırmızı", "Mavi", "Yeşil", 
            "Kahverengi", "Lacivert", "Bordo", "Bej", "Diğer"
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colors) { color ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectColor(color) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (color == state.selectedColor) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        color,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (color == state.selectedColor) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DamageSelectionStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    val damageStatuses = listOf("Orijinal", "Boyalı", "Lokal", "Değişmiş", "Belirtilmemiş")
    val parts = listOf(
        "Ön Tampon" to state.frontBumper,
        "Ön Kaput" to state.frontHood,
        "Tavan" to state.roof,
        "Ön Sağ Kapı" to state.frontRightDoor,
        "Arka Sağ Kapı" to state.rearRightDoor,
        "Ön Sol Çamurluk" to state.frontLeftMudguard,
        "Ön Sol Kapı" to state.frontLeftDoor,
        "Arka Sol Kapı" to state.rearLeftDoor,
        "Arka Sol Çamurluk" to state.rearLeftMudguard,
        "Arka Tampon" to state.rearBumper
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Hasar Bilgileri",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        parts.forEach { (part, currentStatus) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        part,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        damageStatuses.forEach { status ->
                            SelectionChip(
                                text = status,
                                selected = currentStatus == status,
                                onClick = {
                                    when (part) {
                                        "Ön Tampon" -> viewModel.updateDamageInfo(frontBumper = status)
                                        "Ön Kaput" -> viewModel.updateDamageInfo(frontHood = status)
                                        "Tavan" -> viewModel.updateDamageInfo(roof = status)
                                        "Ön Sağ Kapı" -> viewModel.updateDamageInfo(frontRightDoor = status)
                                        "Arka Sağ Kapı" -> viewModel.updateDamageInfo(rearRightDoor = status)
                                        "Ön Sol Çamurluk" -> viewModel.updateDamageInfo(frontLeftMudguard = status)
                                        "Ön Sol Kapı" -> viewModel.updateDamageInfo(frontLeftDoor = status)
                                        "Arka Sol Kapı" -> viewModel.updateDamageInfo(rearLeftDoor = status)
                                        "Arka Sol Çamurluk" -> viewModel.updateDamageInfo(rearLeftMudguard = status)
                                        "Arka Tampon" -> viewModel.updateDamageInfo(rearBumper = status)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineDetailsStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    var enginePower by remember { mutableStateOf(state.selectedEnginePower?.toString() ?: "") }
    var engineVolume by remember { mutableStateOf(state.selectedEngineVolume?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Motor Bilgileri",
            style = MaterialTheme.typography.titleLarge,
            color = appRed
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = enginePower,
            onValueChange = { 
                enginePower = it.filter { char -> char.isDigit() }
                if (enginePower.isNotEmpty() && engineVolume.isNotEmpty()) {
                    viewModel.selectEngineDetails(
                        power = enginePower.toLong(),
                        volume = engineVolume.toLong()
                    )
                }
            },
            label = { Text("Motor Gücü") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appRed,
                focusedLabelColor = appRed
            ),
            prefix = { Text("HP ") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = engineVolume,
            onValueChange = { 
                engineVolume = it.filter { char -> char.isDigit() }
                if (enginePower.isNotEmpty() && engineVolume.isNotEmpty()) {
                    viewModel.selectEngineDetails(
                        power = enginePower.toLong(),
                        volume = engineVolume.toLong()
                    )
                }
            },
            label = { Text("Motor Hacmi") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appRed,
                focusedLabelColor = appRed
            ),
            prefix = { Text("CC ") }
        )
    }
}

@Composable
private fun TramerAmountStep(
    state: PricePredictionState,
    viewModel: PricePredictionViewModel
) {
    var tramerAmount by remember { mutableStateOf(state.selectedTramerAmount?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Tramer Tutarı",
            style = MaterialTheme.typography.titleLarge,
            color = appRed
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tramerAmount,
            onValueChange = { 
                tramerAmount = it.filter { char -> char.isDigit() }
                viewModel.selectTramerAmount(tramerAmount.toLongOrNull() ?: 0L)
            },
            label = { Text("Tramer Tutarı") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appRed,
                focusedLabelColor = appRed
            ),
            prefix = { Text("₺ ") }
        )
    }
}

@Composable
private fun PriceResultCard(
    result: PricePredictionResponse,
    onNewPrediction: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var currentPrice by remember { mutableStateOf(0f) }
    val targetPrice = result.tahminiFiyat.toFloat()
    
    // Animasyon değerleri
    val infiniteTransition = rememberInfiniteTransition(label = "price")
    val maePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mae"
    )

    LaunchedEffect(Unit) {
        delay(300) // İçeriğin görünmesi için kısa bir gecikme
        showContent = true
        
        // Fiyat animasyonu
        animate(
            initialValue = 0f,
            targetValue = targetPrice,
            animationSpec = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            currentPrice = value
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = if (showContent) 1f else 0.8f
                    scaleY = if (showContent) 1f else 0.8f
                    alpha = if (showContent) 1f else 0f
                },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Araç Bilgileri
                Text(
                    text = "Tahmini Değer",
                    style = MaterialTheme.typography.titleLarge,
                    color = appRed,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Fiyat Gösterimi
                Text(
                    text = "₺${String.format("%,d", currentPrice.toLong())}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // MAE Gösterimi
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = maePulse
                            scaleY = maePulse
                        },
                    color = appRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tahmin Sapması",
                            style = MaterialTheme.typography.titleMedium,
                            color = appRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "±",
                                style = MaterialTheme.typography.headlineMedium,
                                color = appRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₺${String.format("%,d", result.mae.toLong())}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = appRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Bu değer, tahminin ortalama sapma miktarını gösterir",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Yeni Tahmin Butonu
        Button(
            onClick = onNewPrediction,
            colors = ButtonDefaults.buttonColors(containerColor = appRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    alpha = if (showContent) 1f else 0f
                }
        ) {
            Text(
                text = "Yeni Tahmin Yap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

