package com.carwise.android.view.listing

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.data.model.*
import com.carwise.android.ui.theme.appRed
import com.carwise.android.view.components.ListingCard
import com.carwise.android.view.components.ShimmerListingCard
import com.carwise.android.viewmodel.listing.ListingsState
import com.carwise.android.viewmodel.listing.ListingsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import com.carwise.android.view.components.ListingCardType
import androidx.compose.ui.platform.LocalContext
import com.carwise.android.data.local.CardTypePreference

// Sabit listeler
private val bodyTypes = listOf(
    "Sedan",
    "Hatchback",
    "Station Wagon",
    "SUV",
    "Crossover",
    "Coupe",
    "Cabrio",
    "Pickup",
    "Van",
    "Minivan"
)

private val transmissionTypes = listOf(
    "Manuel",
    "Otomatik",
    "Yarı Otomatik",
    "CVT"
)

private val fuelTypes = listOf(
    "Benzin",
    "Dizel",
    "LPG",
    "Elektrik",
    "Hibrit",
    "Benzin & LPG"
)

private val driveTypes = listOf(
    "Önden Çekiş",
    "Arkadan İtiş",
    "4x4",
    "AWD"
)

private val colors = listOf(
    "Siyah",
    "Beyaz",
    "Gri",
    "Gümüş",
    "Kırmızı",
    "Mavi",
    "Yeşil",
    "Kahverengi",
    "Sarı",
    "Turuncu",
    "Mor",
    "Lacivert"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    navController: NavController,
    viewModel: ListingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cardTypeFlow = remember { CardTypePreference.getCardTypeFlow(context) }
    var cardType by remember { mutableStateOf(ListingCardType.COMPACT) }

    LaunchedEffect(Unit) {
        cardTypeFlow.collect { savedType ->
            cardType = savedType
        }
    }

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isLoading)
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSortMenu by remember { mutableStateOf(false) }
    val cities by viewModel.cities.collectAsState()
    val districts by viewModel.districts.collectAsState()
    val neighborhoods by viewModel.neighborhoods.collectAsState()

    // Filtre sayacı hesaplama
    val activeFilterCount = remember(state.filters) {
        var count = 0
        val activeFilters = mutableListOf<String>()
        
        with(state.filters) {
            // Marka, seri, model filtreleri
            if (brandId.isNotEmpty()) {
                count++
                activeFilters.add("Brand: $brandId")
            }
            if (seriesId.isNotEmpty()) {
                count++
                activeFilters.add("Series: $seriesId")
            }
            if (modelId.isNotEmpty()) {
                count++
                activeFilters.add("Model: $modelId")
            }
            
            // Araç özellikleri filtreleri
            if (bodyType.isNotEmpty()) {
                count++
                activeFilters.add("BodyType: $bodyType")
            }
            if (driveType.isNotEmpty()) {
                count++
                activeFilters.add("DriveType: $driveType")
            }
            if (transmissionType.isNotEmpty()) {
                count++
                activeFilters.add("Transmission: $transmissionType")
            }
            if (fuelType.isNotEmpty()) {
                count++
                activeFilters.add("Fuel: $fuelType")
            }
            if (color.isNotEmpty()) {
                count++
                activeFilters.add("Color: $color")
            }
            
            // Konum filtreleri
            if (city.isNotEmpty()) {
                count++
                activeFilters.add("City: $city")
            }
            if (district.isNotEmpty()) {
                count++
                activeFilters.add("District: $district")
            }
            if (neighborhood.isNotEmpty()) {
                count++
                activeFilters.add("Neighborhood: $neighborhood")
            }
            
            // Aralık filtreleri - sadece anlamlı değerleri say
            // Fiyat aralığı: min > 0 veya max < Int.MAX_VALUE
            if (priceRange.first > 0 || (priceRange.second > 0 && priceRange.second < Int.MAX_VALUE)) {
                count++
                activeFilters.add("Price: ${priceRange.first}-${priceRange.second}")
            }
            
            // Yıl aralığı: min > 0 veya max < 2024
            if (yearRange.first > 0 || (yearRange.second > 0 && yearRange.second < 2024)) {
                count++
                activeFilters.add("Year: ${yearRange.first}-${yearRange.second}")
            }
            
            // Kilometre aralığı: min > 0 veya max < Int.MAX_VALUE
            if (kilometersRange.first > 0 || (kilometersRange.second > 0 && kilometersRange.second < Int.MAX_VALUE)) {
                count++
                activeFilters.add("KM: ${kilometersRange.first}-${kilometersRange.second}")
            }
            
            // Motor gücü aralığı: min > 0 veya max < Int.MAX_VALUE
            if (enginePowerRange.first > 0 || (enginePowerRange.second > 0 && enginePowerRange.second < Int.MAX_VALUE)) {
                count++
                activeFilters.add("Power: ${enginePowerRange.first}-${enginePowerRange.second}")
            }
            
            // Motor hacmi aralığı: min > 0 veya max < Int.MAX_VALUE
            if (engineVolumeRange.first > 0 || (engineVolumeRange.second > 0 && engineVolumeRange.second < Int.MAX_VALUE)) {
                count++
                activeFilters.add("Volume: ${engineVolumeRange.first}-${engineVolumeRange.second}")
            }
            
            // Hasar durumu
            if (heavyDamage != null) {
                count++
                activeFilters.add("HeavyDamage: $heavyDamage")
            }
        }

        // Aktif filtreleri logla
        Log.d("FilterCount", "Active Filters (${activeFilters.size}): ${activeFilters.joinToString(", ")}")
        Log.d("FilterCount", "Raw Filter Values: ${state.filters}")
        
        count
    }

    // Pagination scroll trigger
    LaunchedEffect(listState, state.isLoading, state.isPaginating, state.endReached) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            Pair(totalItemCount, lastVisibleItemIndex)
        }.collectLatest { (totalItemCount, lastVisibleItemIndex) ->
            if (!state.isLoading && !state.isPaginating && !state.endReached &&
                totalItemCount > 0 && lastVisibleItemIndex >= totalItemCount - 6
            ) {
                viewModel.loadMoreListings()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tüm İlanlar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = appRed
                        )
                    }
                },
                actions = {
                    // Card type toggle button
                    IconButton(
                        onClick = {
                            val newType = if (cardType == ListingCardType.COMPACT)
                                ListingCardType.DETAILED else ListingCardType.COMPACT
                            cardType = newType
                            scope.launch {
                                CardTypePreference.setCardType(context, newType)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (cardType == ListingCardType.COMPACT)
                                Icons.Default.ViewModule else Icons.Default.ViewList,
                            contentDescription = if (cardType == ListingCardType.COMPACT)
                                "Detaylı görünüme geç" else "Kompakt görünüme geç",
                            tint = appRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFilterSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Filtrele",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        if (activeFilterCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(activeFilterCount.toString(), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                // Dikey ayraç
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFFE0E0E0))
                )
                // Sırala butonu
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSortMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sırala",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .align(Alignment.TopCenter)
                ) {
                    val sortOptions = listOf(
                        Triple(SortBy.PRICE, SortOrder.DESC, "Fiyata göre (Önce en yüksek)"),
                        Triple(SortBy.PRICE, SortOrder.ASC, "Fiyata göre (Önce en düşük)"),
                        Triple(SortBy.CREATED_AT, SortOrder.DESC, "Tarihe göre (Önce en yeni ilan)"),
                        Triple(SortBy.CREATED_AT, SortOrder.ASC, "Tarihe göre (Önce en eski ilan)"),
                        Triple(SortBy.KILOMETERS, SortOrder.ASC, "Km'ye göre (Önce en düşük)"),
                        Triple(SortBy.KILOMETERS, SortOrder.DESC, "Km'ye göre (Önce en yüksek)"),
                        Triple(SortBy.YEAR, SortOrder.ASC, "Yıla göre (Önce en eski)"),
                        Triple(SortBy.YEAR, SortOrder.DESC, "Yıla göre (Önce en yeni)")
                    )
                    sortOptions.forEach { (sortBy, sortOrder, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.updateSorting(sortBy, sortOrder)
                                showSortMenu = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refreshListings() }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Header with total count
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isLoading) "Yükleniyor..." else "Toplam ${state.total} ilan bulundu",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    // Listings
                    if (state.isLoading) {
                        items(8) { ShimmerListingCard() }
                    } else {
                        items(
                            items = state.listings,
                            key = { it.id }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onListingClick = { id ->
                                    navController.navigate("listing_detail/$id")
                                },
                                cardType = cardType
                            )
                        }
                        // Loading indicator for pagination
                        if (state.isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = appRed)
                                }
                            }
                        }
                    }
                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        // Filtre BottomSheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = filterSheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                FilterScreen(
                    filters = state.filters,
                    onFiltersChange = { viewModel.applyFilters(it) },
                    onApply = { showFilterSheet = false },
                    onDismiss = { showFilterSheet = false },
                    cities = cities,
                    districts = districts,
                    neighborhoods = neighborhoods,
                    onCitySelected = viewModel::onCitySelected,
                    onDistrictSelected = viewModel::onDistrictSelected,
                    onNeighborhoodSelected = viewModel::onNeighborhoodSelected,
                    brands = state.brands,
                    onBrandSelected = viewModel::selectBrand,
                    selectedBrand = state.selectedBrand,
                    selectedSeries = state.selectedSeries,
                    selectedModel = state.selectedModel,
                    onSeriesSelected = viewModel::selectSeries,
                    onModelSelected = viewModel::selectModel,
                    onClearAllFilters = viewModel::clearAllFilters
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    filters: ListingFilters,
    onFiltersChange: (ListingFilters) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    cities: List<String>,
    districts: List<District>,
    neighborhoods: List<String>,
    onCitySelected: (String) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onNeighborhoodSelected: (String) -> Unit,
    brands: List<Brand>,
    onBrandSelected: (Brand) -> Unit,
    selectedBrand: Brand?,
    selectedSeries: GetBrandSeries?,
    selectedModel: GetBrandSeriesModels?,
    onSeriesSelected: (GetBrandSeries) -> Unit,
    onModelSelected: (GetBrandSeriesModels) -> Unit,
    onClearAllFilters: () -> Unit
) {
    var showPriceDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }
    var showFuelDialog by remember { mutableStateOf(false) }
    var showTransmissionDialog by remember { mutableStateOf(false) }
    var showBodyTypeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showKmDialog by remember { mutableStateOf(false) }
    var showEnginePowerDialog by remember { mutableStateOf(false) }
    var showEngineVolumeDialog by remember { mutableStateOf(false) }
    var showDriveTypeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val headerAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 50) 0.95f else 1f,
        animationSpec = tween(300),
        label = "header_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F3F4)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header with blur effect
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = headerAlpha),
                tonalElevation = if (scrollState.value > 50) 8.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Filtreler",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        )
                        Text(
                            "Arama kriterlerinizi belirleyin",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF666666)
                            )
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onClearAllFilters,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF4444).copy(alpha = 0.1f),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ClearAll,
                                    contentDescription = "Temizle",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFF4444)
                                )
                                Text(
                                    "Temizle",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF4444)
                                    )
                                )
                            }
                        }
                    }
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Kapat",
                            modifier = Modifier.padding(8.dp),
                            tint = Color(0xFF666666)
                        )
                    }
                }
            }

            // Content with enhanced styling
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location Section
                item {
                    Text(
                        "Marka Bilgileri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }

                item {
                    EnhancedFilterCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Brand Selection
                            EnhancedBrandDropdown(
                                label = "Marka",
                                value = selectedBrand?.name ?: "",
                                placeholder = "Marka seçin",
                                icon = Icons.Default.DirectionsCar,
                                brands = brands,
                                onBrandSelected = onBrandSelected
                            )

                            // Series Selection
                            AnimatedVisibility(
                                visible = selectedBrand != null,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                EnhancedSeriesDropdown(
                                    label = "Seri",
                                    value = selectedSeries?.name ?: "",
                                    placeholder = "Seri seçin",
                                    icon = Icons.Default.DirectionsCar,
                                    series = selectedBrand?.series ?: emptyList(),
                                    onSeriesSelected = onSeriesSelected
                                )
                            }

                            // Model Selection
                            AnimatedVisibility(
                                visible = selectedSeries != null && selectedSeries.models.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                EnhancedModelDropdown(
                                    label = "Model",
                                    value = selectedModel?.name ?: "",
                                    placeholder = "Model seçin",
                                    icon = Icons.Default.DirectionsCar,
                                    models = selectedSeries?.models ?: emptyList(),
                                    onModelSelected = onModelSelected
                                )
                            }
                        }
                    }
                }

                // Location Section
                item {
                    Text(
                        "Konum Bilgileri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }

                item {
                    EnhancedFilterCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Enhanced City Selection
                            EnhancedLocationDropdown(
                                label = "Şehir",
                                value = filters.city,
                                placeholder = "Şehir seçin",
                                icon = Icons.Default.LocationCity,
                                options = cities,
                                onOptionSelected = { onCitySelected(it as String) }
                            )

                            // Enhanced District Selection
                            AnimatedVisibility(
                                visible = filters.city.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                EnhancedLocationDropdown(
                                    label = "İlçe",
                                    value = filters.district,
                                    placeholder = "İlçe seçin",
                                    icon = Icons.Default.LocationOn,
                                    options = districts.map { it.name },
                                    onOptionSelected = { selectedName ->
                                        districts.find { it.name == selectedName }?.let { district ->
                                            onDistrictSelected(district)
                                        }
                                    }
                                )
                            }

                            // Enhanced Neighborhood Selection
                            AnimatedVisibility(
                                visible = filters.district.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                EnhancedLocationDropdown(
                                    label = "Mahalle",
                                    value = filters.neighborhood,
                                    placeholder = "Mahalle seçin",
                                    icon = Icons.Default.Place,
                                    options = neighborhoods,
                                    onOptionSelected = { onNeighborhoodSelected(it as String) }
                                )
                            }
                        }
                    }
                }

                // Filter Categories with cards
                item {
                    Text(
                        "Araç Özellikleri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    EnhancedFilterCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            EnhancedFilterItem(
                                icon = Icons.Default.AttachMoney,
                                title = "Fiyat Aralığı",
                                value = formatPrice(filters.priceRange.first) + " - " + formatPrice(filters.priceRange.second),
                                color = Color(0xFF4CAF50)
                            ) { showPriceDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.CalendarToday,
                                title = "Model Yılı",
                                value = (if (filters.yearRange.first > 0) filters.yearRange.first.toString() else "") +
                                        " - " + (if (filters.yearRange.second < 2024) filters.yearRange.second.toString() else ""),
                                color = Color(0xFF2196F3)
                            ) { showYearDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.LocalGasStation,
                                title = "Yakıt Tipi",
                                value = filters.fuelType.ifEmpty { "Seçilmedi" },
                                color = Color(0xFFFF9800)
                            ) { showFuelDialog = true }
                        }
                    }
                }

                item {
                    EnhancedFilterCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            EnhancedFilterItem(
                                icon = Icons.Default.Settings,
                                title = "Vites Tipi",
                                value = filters.transmissionType.ifEmpty { "Seçilmedi" },
                                color = Color(0xFF9C27B0)
                            ) { showTransmissionDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.DirectionsCar,
                                title = "Kasa Tipi",
                                value = filters.bodyType.ifEmpty { "Seçilmedi" },
                                color = Color(0xFF607D8B)
                            ) { showBodyTypeDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.Palette,
                                title = "Renk",
                                value = filters.color.ifEmpty { "Seçilmedi" },
                                color = Color(0xFFE91E63)
                            ) { showColorDialog = true }
                        }
                    }
                }

                item {
                    EnhancedFilterCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            EnhancedFilterItem(
                                icon = Icons.Default.Speed,
                                title = "Kilometre",
                                value = formatNumber(filters.kilometersRange.first) + " - " +
                                        formatNumber(filters.kilometersRange.second) + " km",
                                color = Color(0xFF795548)
                            ) { showKmDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.ElectricBolt,
                                title = "Motor Gücü",
                                value = (if (filters.enginePowerRange.first > 0) filters.enginePowerRange.first.toString() else "") +
                                        " - " + (if (filters.enginePowerRange.second < 1000) filters.enginePowerRange.second.toString() else "") + " HP",
                                color = Color(0xFFFF5722)
                            ) { showEnginePowerDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.Engineering,
                                title = "Motor Hacmi",
                                value = (if (filters.engineVolumeRange.first > 0) filters.engineVolumeRange.first.toString() else "") +
                                        " - " + (if (filters.engineVolumeRange.second < 10000) filters.engineVolumeRange.second.toString() else "") + " cc",
                                color = Color(0xFF3F51B5)
                            ) { showEngineVolumeDialog = true }

                            EnhancedFilterItem(
                                icon = Icons.Default.DriveEta,
                                title = "Çekiş Tipi",
                                value = filters.driveType.ifEmpty { "Seçilmedi" },
                                color = Color(0xFF009688)
                            ) { showDriveTypeDialog = true }
                        }
                    }
                }


            }

            // Enhanced Apply Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4444)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Sonuçları Listele",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }

    // Dialogs remain the same
    if (showPriceDialog) {
        PriceRangeDialog(
            initialMin = filters.priceRange.first,
            initialMax = filters.priceRange.second,
            onDismiss = { showPriceDialog = false },
            onConfirm = { min, max ->
                onFiltersChange(filters.copy(priceRange = Pair(min, max)))
                showPriceDialog = false
            }
        )
    }

    if (showYearDialog) {
        YearRangeDialog(
            initialMin = filters.yearRange.first,
            initialMax = filters.yearRange.second,
            onDismiss = { showYearDialog = false },
            onConfirm = { min, max ->
                onFiltersChange(filters.copy(yearRange = Pair(min, max)))
                showYearDialog = false
            }
        )
    }
    if (showFuelDialog) {
        SimpleListDialog(
            title = "Yakıt Tipi Seçin",
            options = fuelTypes,
            selected = filters.fuelType,
            onDismiss = { showFuelDialog = false },
            onSelect = { selected ->
                onFiltersChange(filters.copy(fuelType = selected))
                showFuelDialog = false
            }
        )
    }
    if (showTransmissionDialog) {
        SimpleListDialog(
            title = "Vites Tipi Seçin",
            options = transmissionTypes,
            selected = filters.transmissionType,
            onDismiss = { showTransmissionDialog = false },
            onSelect = { selected ->
                onFiltersChange(filters.copy(transmissionType = selected))
                showTransmissionDialog = false
            }
        )
    }
    if (showBodyTypeDialog) {
        SimpleListDialog(
            title = "Kasa Tipi Seçin",
            options = bodyTypes,
            selected = filters.bodyType,
            onDismiss = { showBodyTypeDialog = false },
            onSelect = { selected ->
                onFiltersChange(filters.copy(bodyType = selected))
                showBodyTypeDialog = false
            }
        )
    }
    if (showColorDialog) {
        SimpleListDialog(
            title = "Renk Seçin",
            options = colors,
            selected = filters.color,
            onDismiss = { showColorDialog = false },
            onSelect = { selected ->
                onFiltersChange(filters.copy(color = selected))
                showColorDialog = false
            }
        )
    }
    if (showKmDialog) {
        RangeDialog(
            title = "KM Aralığı",
            initialMin = filters.kilometersRange.first,
            initialMax = filters.kilometersRange.second,
            onDismiss = { showKmDialog = false },
            onConfirm = { min, max ->
                onFiltersChange(filters.copy(kilometersRange = Pair(min, max)))
                showKmDialog = false
            },
            unit = "km"
        )
    }
    if (showEnginePowerDialog) {
        RangeDialog(
            title = "Motor Gücü (HP)",
            initialMin = filters.enginePowerRange.first,
            initialMax = filters.enginePowerRange.second,
            onDismiss = { showEnginePowerDialog = false },
            onConfirm = { min, max ->
                onFiltersChange(filters.copy(enginePowerRange = Pair(min, max)))
                showEnginePowerDialog = false
            },
            unit = "HP"
        )
    }
    if (showEngineVolumeDialog) {
        RangeDialog(
            title = "Motor Hacmi (cc)",
            initialMin = filters.engineVolumeRange.first,
            initialMax = filters.engineVolumeRange.second,
            onDismiss = { showEngineVolumeDialog = false },
            onConfirm = { min, max ->
                onFiltersChange(filters.copy(engineVolumeRange = Pair(min, max)))
                showEngineVolumeDialog = false
            },
            unit = "cc"
        )
    }
    if (showDriveTypeDialog) {
        SimpleListDialog(
            title = "Çekiş Tipi Seçin",
            options = driveTypes,
            selected = filters.driveType,
            onDismiss = { showDriveTypeDialog = false },
            onSelect = { selected ->
                onFiltersChange(filters.copy(driveType = selected))
                showDriveTypeDialog = false
            }
        )
    }
}

@Composable
fun EnhancedFilterCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun EnhancedFilterItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedLocationDropdown(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    options: List<String>,
    onOptionSelected: (Any) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF999999)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF4444),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFFFF4444),
                    unfocusedLabelColor = Color(0xFF999999),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBrandDropdown(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    brands: List<Brand>,
    onBrandSelected: (Brand) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBrand = brands.find { it.name == value }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF999999)
                    )
                },
                leadingIcon = {
                    selectedBrand?.imagePath?.let { imagePath ->
                        if (imagePath.isNotEmpty()) {
                            AsyncImage(
                                model = imagePath,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF4444),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFFFF4444),
                    unfocusedLabelColor = Color(0xFF999999),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                brands.forEach { brand ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                brand.imagePath?.let { imagePath ->
                                    AsyncImage(
                                        model = imagePath,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    brand.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onBrandSelected(brand)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSeriesDropdown(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    series: List<GetBrandSeries>,
    onSeriesSelected: (GetBrandSeries) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF999999)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF4444),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFFFF4444),
                    unfocusedLabelColor = Color(0xFF999999),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                series.forEach { series ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                series.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        },
                        onClick = {
                            onSeriesSelected(series)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedModelDropdown(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    models: List<GetBrandSeriesModels>,
    onModelSelected: (GetBrandSeriesModels) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF999999)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF4444),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFFFF4444),
                    unfocusedLabelColor = Color(0xFF999999),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                model.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        },
                        onClick = {
                            onModelSelected(model)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatPrice(price: Int): String {
    return if (price == 0) "" else String.format("%,d", price).replace(",", ".")
}

@SuppressLint("DefaultLocale")
private fun formatNumber(number: Int): String {
    return if (number == 0) "" else String.format("%,d", number).replace(",", ".")
}

@Composable
fun PriceRangeDialog(initialMin: Int, initialMax: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var min by remember { mutableStateOf(if (initialMin > 0) initialMin.toString() else "") }
    var max by remember { mutableStateOf(if (initialMax > 0) initialMax.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fiyat Aralığı") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = min,
                    onValueChange = { min = it.filter { c -> c.isDigit() } },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = max,
                    onValueChange = { max = it.filter { c -> c.isDigit() } },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(min.toIntOrNull() ?: 0, max.toIntOrNull() ?: Int.MAX_VALUE)
            }) { Text("Uygula") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun YearRangeDialog(initialMin: Int, initialMax: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var min by remember { mutableStateOf(if (initialMin > 0) initialMin.toString() else "") }
    var max by remember { mutableStateOf(if (initialMax > 0) initialMax.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yıl Aralığı") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = min,
                    onValueChange = { min = it.filter { c -> c.isDigit() } },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = max,
                    onValueChange = { max = it.filter { c -> c.isDigit() } },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(min.toIntOrNull() ?: 0, max.toIntOrNull() ?: 2024)
            }) { Text("Uygula") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun SimpleListDialog(title: String, options: List<String>, selected: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) }
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

@Composable
fun RangeDialog(title: String, initialMin: Int, initialMax: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit, unit: String) {
    var min by remember { mutableStateOf(if (initialMin > 0) initialMin.toString() else "") }
    var max by remember { mutableStateOf(if (initialMax > 0) initialMax.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = min,
                    onValueChange = { min = it.filter { c -> c.isDigit() } },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text(unit) }
                )
                OutlinedTextField(
                    value = max,
                    onValueChange = { max = it.filter { c -> c.isDigit() } },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text(unit) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(min.toIntOrNull() ?: 0, max.toIntOrNull() ?: Int.MAX_VALUE)
            }) { Text("Uygula") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
} 

