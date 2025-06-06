package com.carwise.android.view.update_listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.carwise.android.R
import com.carwise.android.data.model.*
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.update_listing.UpdateListingViewModel
import com.carwise.android.viewmodel.update_listing.NavigationEvent
import com.carwise.android.viewmodel.update_listing.ImageUploadState
import com.carwise.android.viewmodel.update_listing.UpdateListingState
import java.text.NumberFormat
import java.util.*
import android.text.Html
import android.util.Log
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import com.carwise.android.components.RichTextEditorComponent
import com.carwise.android.view.components.PreviewStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateListingScreen(
    navController: NavController,
    listingId: String,
    viewModel: UpdateListingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("tr", "TR")) }
    numberFormat.maximumFractionDigits = 0

    val steps = listOf(
        "Marka",
        "Seri",
        "Model",
        "Yıl",
        "Kasa",
        "Yakıt",
        "Detaylar",
        "Konum",
        "Fiyat",
        "Açıklama",
        "Fotoğraf",
        "Önizleme"
    )

    val stepIcons = listOf(
        Icons.Default.DirectionsCar,
        Icons.Default.DirectionsCar,
        Icons.Default.DirectionsCar,
        Icons.Default.DateRange,
        Icons.Default.Build,
        Icons.Default.LocalGasStation,
        Icons.Default.Settings,
        Icons.Default.LocationOn,
        Icons.Default.AttachMoney,
        Icons.Default.Description,
        Icons.Default.PhotoCamera,
        Icons.Default.CheckCircle
    )

    // Navigation event handling
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToListingDetail -> {
                navController.navigate("listing_detail/${(navigationEvent as NavigationEvent.NavigateToListingDetail).listingId}") {
                    popUpTo("update_listing/{listingId}") { inclusive = true }
                }
                viewModel.onNavigationHandled()
            }
            null -> {}
        }
    }

    // Load initial data
    LaunchedEffect(listingId) {
        viewModel.loadInitialData(listingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlanı Düzenle", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = appRed)
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
            if (state.isLoading && state.currentStep == 0 && state.brands.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (state.currentStep) {
                            0 -> BrandSelectionStep(state.brands, state.selectedBrand, viewModel::selectBrand, state.isLoading, state.error)
                            1 -> SeriesSelectionStep(state, viewModel)
                            2 -> ModelSelectionStep(state, viewModel)
                            3 -> YearSelectionStep(state, viewModel)
                            4 -> BodyTypeSelectionStep(state, viewModel)
                            5 -> FuelTypeSelectionStep(state, viewModel)
                            6 -> DetailsSelectionStep(state, viewModel)
                            7 -> LocationSelectionStep(
                                state = state,
                                onCitySelected = viewModel::onCitySelected,
                                onDistrictSelected = viewModel::onDistrictSelected,
                                onNeighborhoodSelected = viewModel::onNeighborhoodSelected,
                                cities = viewModel.cities.collectAsState().value,
                                districts = viewModel.districts.collectAsState().value,
                                neighborhoods = viewModel.neighborhoods.collectAsState().value
                            )
                            8 -> PriceSelectionStep(state, viewModel)
                            9 -> DescriptionStep(state, viewModel)
                            10 -> PhotoSelectionStep(state, viewModel)
                            11 -> PreviewStep(
                                title = state.title,
                                price = state.selectedPrice,
                                currency = state.selectedCurrency,
                                brand = state.selectedBrand?.name,
                                series = state.selectedSeries?.name,
                                model = state.selectedModel?.name,
                                year = state.selectedYear,
                                bodyType = state.selectedBodyType,
                                fuelType = state.selectedFuelType,
                                transmissionType = state.selectedTransmissionType,
                                driveType = state.selectedDriveType,
                                color = state.selectedColor,
                                enginePower = state.selectedEnginePower,
                                engineVolume = state.selectedEngineVolume,
                                kilometers = state.selectedKilometers,
                                city = state.selectedCity,
                                district = state.selectedDistrict?.name,
                                neighborhood = state.selectedNeighborhood,
                                description = state.description,
                                images = state.selectedImages,
                                isLoading = state.isLoading,
                                onUpdateClick = { viewModel.updateListing() },
                                isUpdateScreen = true
                            )
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
                        if (state.currentStep > 0 && state.currentStep < 11 ) {
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

                        } else if (state.currentStep == 11) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                OutlinedButton(
                                    onClick = { viewModel.goToPreviousStep() },
                                    modifier = Modifier.size(48.dp), // Kare şekil için sabit boyut
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = appRed
                                    ),
                                    border = BorderStroke(1.dp, appRed),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp) // İç padding'i kaldır
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Geri",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.updateListing() },
                                    modifier = Modifier.weight(1f), // Kalan alanı kapla
                                    colors = ButtonDefaults.buttonColors(containerColor = appRed),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !state.isLoading
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("İlanı güncelle", color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Next Button
                        if (state.currentStep < steps.size - 1) {
                            val isNextEnabled = when (state.currentStep) {
                                0 -> state.selectedBrand != null
                                1 -> state.selectedSeries != null
                                2 -> state.selectedModel != null
                                3 -> state.selectedYear != null
                                4 -> state.selectedBodyType != null
                                5 -> state.selectedFuelType != null
                                6 -> {
                                    val hasTransmission = state.selectedTransmissionType != null
                                    val hasDriveType = state.selectedDriveType != null
                                    val hasColor = state.selectedColor != null
                                    val hasValidKilometers = state.selectedKilometers?.let { it > 0 } ?: false
                                    hasTransmission && hasDriveType && hasColor && hasValidKilometers
                                }
                                7 -> {
                                    val hasCity = state.selectedCity != null
                                    val hasDistrict = state.selectedDistrict != null
                                    hasCity && hasDistrict
                                }
                                8 -> {
                                    val price = state.selectedPrice
                                    price != null && price > 0 && state.title.isNotEmpty()
                                }
                                9 -> true // Açıklama artık zorunlu değil
                                10 -> true // Fotoğraf adımı opsiyonel
                                else -> true
                            }

                            Button(
                                onClick = { 
                                    if (!state.isLoading) {
                                        viewModel.goToNextStep()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = appRed),
                                shape = RoundedCornerShape(12.dp),
                                enabled = isNextEnabled && !state.isLoading
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("İleri")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = { /* Dialog can't be dismissed */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = appRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "İlan Güncellendi",
                    style = MaterialTheme.typography.titleLarge,
                    color = appRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "İlanınız başarıyla güncellendi.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = { /* Navigation is handled by the navigation event */ },
                    colors = ButtonDefaults.buttonColors(containerColor = appRed)
                ) {
                    Text("Tamam", color = Color.White)
                }
            },
            containerColor = Color.White,
            titleContentColor = appRed,
            textContentColor = Color.Gray
        )
    }
}

@Composable
private fun BrandSelectionStep(
    brands: List<Brand>,
    selectedBrand: Brand?,
    onBrandSelected: (Brand) -> Unit,
    isLoading: Boolean,
    error: String?
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

        if (isLoading) {
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
                items(brands) { brand ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBrandSelected(brand) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (brand == selectedBrand) 
                                appRed.copy(alpha = 0.1f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            brand.name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (brand == selectedBrand) appRed else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
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
                if (brand.series.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Bu marka için seri bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(brand.series) { series ->
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
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
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
                if (series.models.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Bu seri için model bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(series.models) { model ->
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
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Yıl Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val years = (2024 downTo 1950).toList()
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            years.forEach { year ->
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
private fun BodyTypeSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Kasa Tipi Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val bodyTypes = listOf("Sedan", "Hatchback", "SUV", "Station Wagon", "Coupe", "Cabrio", "Pickup")
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bodyTypes.forEach { bodyType ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectBodyType(bodyType) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (bodyType == state.selectedBodyType) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        bodyType,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (bodyType == state.selectedBodyType) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun FuelTypeSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Yakıt Tipi Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val fuelTypes = listOf("Benzin", "Dizel", "LPG", "Elektrik", "Hibrit")
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fuelTypes.forEach { fuelType ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectFuelType(fuelType) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (fuelType == state.selectedFuelType) 
                            appRed.copy(alpha = 0.1f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        fuelType,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (fuelType == state.selectedFuelType) appRed else Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Detayları Girin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Engine Details
        var enginePower by remember { mutableStateOf(state.selectedEnginePower?.toString() ?: "") }
        var engineVolume by remember { mutableStateOf(state.selectedEngineVolume?.toString() ?: "") }
        var kilometers by remember { mutableStateOf(state.selectedKilometers?.toString() ?: "") }
        
        // Engine Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Transmission Type
                Text(
                    "Vites Tipi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val transmissionTypes = listOf("Manuel", "Otomatik", "Yarı Otomatik")
                    transmissionTypes.forEach { type ->
                        SelectionChip(
                            text = type,
                            selected = state.selectedTransmissionType == type,
                            onClick = { viewModel.selectTransmissionType(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drive Type
                Text(
                    "Çekiş Tipi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val driveTypes = listOf("Önden Çekiş", "Arkadan İtiş", "4x4")
                    driveTypes.forEach { type ->
                        SelectionChip(
                            text = type,
                            selected = state.selectedDriveType == type,
                            onClick = { viewModel.selectDriveType(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color
                Text(
                    "Renk",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf(
                        "Siyah", "Beyaz", "Gri", "Kırmızı", "Mavi", "Yeşil",
                        "Kahverengi", "Bordo", "Lacivert", "Turuncu", "Sarı"
                    )
                    colors.forEach { color ->
                        SelectionChip(
                            text = color,
                            selected = state.selectedColor == color,
                            onClick = { viewModel.selectColor(color) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Engine Power
                OutlinedTextField(
                    value = enginePower,
                    onValueChange = { 
                        enginePower = it
                        val power = it.toLongOrNull() ?: 0L
                        val volume = engineVolume.toLongOrNull() ?: 0L
                        val km = kilometers.toLongOrNull() ?: 0L
                        viewModel.selectEngineDetails(power, volume, km)
                    },
                    label = { Text("Motor Gücü (HP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Engine Volume
                OutlinedTextField(
                    value = engineVolume,
                    onValueChange = { 
                        engineVolume = it
                        val power = enginePower.toLongOrNull() ?: 0L
                        val volume = it.toLongOrNull() ?: 0L
                        val km = kilometers.toLongOrNull() ?: 0L
                        viewModel.selectEngineDetails(power, volume, km)
                    },
                    label = { Text("Motor Hacmi (cc)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kilometers
                OutlinedTextField(
                    value = kilometers,
                    onValueChange = { 
                        kilometers = it
                        val power = enginePower.toLongOrNull() ?: 0L
                        val volume = engineVolume.toLongOrNull() ?: 0L
                        val km = it.toLongOrNull() ?: 0L
                        viewModel.selectEngineDetails(power, volume, km)
                    },
                    label = { Text("Kilometre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Heavy Damage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ağır Hasar",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.selectedHeavyDamage ?: false,
                        onCheckedChange = { viewModel.selectHeavyDamage(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = appRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // Damage Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                    "Hasar Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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

                parts.forEach { (part, currentStatus) ->
                    Text(
                        text = part,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelectionStep(
    state: UpdateListingState,
    onCitySelected: (String) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onNeighborhoodSelected: (String) -> Unit,
    cities: List<String>,
    districts: List<District>,
    neighborhoods: List<String>
) {
    var cityExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var neighborhoodExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Konum Bilgileri",
            style = MaterialTheme.typography.titleLarge.copy(
                color = appRed,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // İl Seçimi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "İl",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedCity ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = appRed,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    onCitySelected(city)
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // İlçe Seçimi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "İlçe",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedDistrict?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = appRed,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        districts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district.name) },
                                onClick = {
                                    onDistrictSelected(district)
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Mahalle Seçimi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mahalle",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = neighborhoodExpanded,
                    onExpandedChange = { neighborhoodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedNeighborhood ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = neighborhoodExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = appRed,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = neighborhoodExpanded,
                        onDismissRequest = { neighborhoodExpanded = false }
                    ) {
                        neighborhoods.forEach { neighborhood ->
                            DropdownMenuItem(
                                text = { Text(neighborhood) },
                                onClick = {
                                    onNeighborhoodSelected(neighborhood)
                                    neighborhoodExpanded = false
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
private fun PriceSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    var price by remember { mutableStateOf(state.selectedPrice?.toString() ?: "") }
    var title by remember { mutableStateOf(state.title) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Fiyat ve Başlık",
            style = MaterialTheme.typography.titleLarge.copy(
                color = appRed,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Başlık
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        viewModel.updateTitle(it)
                    },
                    label = { Text("Başlık") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        focusedLabelColor = appRed,
                        cursorColor = appRed
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fiyat
                OutlinedTextField(
                    value = price,
                    onValueChange = { 
                        price = it
                        viewModel.updatePrice(it.toLongOrNull())
                    },
                    label = { Text("Fiyat") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        focusedLabelColor = appRed,
                        cursorColor = appRed
                    ),
                    prefix = { Text("₺ ") }
                )
            }
        }
    }
}

@Composable
private fun DescriptionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Açıklama",
            style = MaterialTheme.typography.titleLarge.copy(
                color = appRed,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                RichTextEditorComponent(
                    value = state.description,
                    onValueChange = {
                        Log.d("DescriptionStep", "onValueChange called with value: $it")
                        viewModel.updateDescription(it) },
                    label = "Açıklama",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun PhotoSelectionStep(
    state: UpdateListingState,
    viewModel: UpdateListingViewModel
) {
    val context = LocalContext.current
    val imageUploadStates by viewModel.imageUploadStates.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Fotoğraf Seçin",
            style = MaterialTheme.typography.titleLarge,
            color = appRed
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "En fazla 20 fotoğraf yükleyebilirsiniz",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Fotoğraf sayısı göstergesi
        val uploadedCount = imageUploadStates.count { it.uploadedUrl != null }
        val loadingCount = imageUploadStates.count { it.isLoading }
        val errorCount = imageUploadStates.count { it.error != null }
        
        Text(
            buildAnnotatedString {
                append("$uploadedCount/20 fotoğraf yüklendi")
                if (loadingCount > 0) {
                    append(" ($loadingCount yükleniyor)")
                }
                if (errorCount > 0) {
                    append(" ($errorCount hata)")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                errorCount > 0 -> appRed
                loadingCount > 0 -> Color.Gray
                uploadedCount >= 20 -> appRed
                else -> Color.Gray
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Fotoğraf ekleme butonu
        Button(
            onClick = { launcher.launch("image/*") },
            enabled = uploadedCount + loadingCount < 20,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = appRed)
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fotoğraf Ekle")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seçilen fotoğraflar grid'i
        if (imageUploadStates.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(imageUploadStates.size) { index ->
                        val uploadState = imageUploadStates[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            // Fotoğraf önizlemesi
                            AsyncImage(
                                model = uploadState.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Yükleme durumu
                            if (uploadState.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            
                            // Hata durumu
                            uploadState.error?.let { error ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = error,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            
                            // Silme butonu
                            IconButton(
                                onClick = { viewModel.removeImage(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fotoğrafı Sil",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Genel hata mesajı
        state.error?.let { error ->
            Text(
                text = error,
                color = appRed,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SelectionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (selected) appRed.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) appRed else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) appRed else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    steps: List<String>,
    stepIcons: List<ImageVector>,
    tint: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, stepName ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < currentStep -> tint.copy(alpha = 0.2f)
                                    index == currentStep -> tint
                                    else -> Color.LightGray.copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    index < currentStep -> tint
                                    index == currentStep -> tint
                                    else -> Color.Gray.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                index < currentStep -> Icons.Default.Check
                                else -> stepIcons[index]
                            },
                            contentDescription = stepName,
                            tint = when {
                                index < currentStep -> tint
                                index == currentStep -> Color.White
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Step Name
                    Text(
                        text = stepName,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            index < currentStep -> tint
                            index == currentStep -> tint
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1
                    )
                }
                
                // Connector Line
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                when {
                                    index < currentStep -> tint
                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun DetailRow(label: String, value: Any) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}
