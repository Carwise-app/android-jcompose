package com.carwise.android.view.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.carwise.android.R
import com.carwise.android.data.model.GetStatsResponse
import com.carwise.android.data.model.UserResponse
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.dashboard.DashboardViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import com.carwise.android.data.model.Brand
import com.carwise.android.data.model.GetBrandSeries
import com.carwise.android.data.model.GetBrandSeriesModels
import com.carwise.android.data.model.Series

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = dashboardState.isLoading)

    var showAddBrandDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Kullanıcılar", "Markalar")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = appRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = appRed
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error handling
                dashboardState.error?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = error,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.clearError() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Kapat",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats Grid
                item {
                    Text(
                        "Genel İstatistikler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    StatsGrid(stats = dashboardState.stats)
                }

                // Tab Row
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTabIndex = index }
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) appRed else Color.Gray
                                    )
                                }
                            }
                        }
                        
                        // Custom indicator
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectedTabIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .background(appRed)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .background(Color.Transparent)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .background(Color.Transparent)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .background(appRed)
                                )
                            }
                        }
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        // Users Tab Content
                        item {
                            UsersTabContent(
                                users = dashboardState.users,
                                isPaginating = dashboardState.isPaginating,
                                endReached = dashboardState.endReached,
                                onDeleteUser = { userId -> viewModel.deleteUser(userId) },
                                onLoadMore = { viewModel.loadUsers() },
                                onUpdateRole = { userId, role -> viewModel.updateUserRole(userId, role) }
                            )
                        }
                    }
                    1 -> {
                        // Brands Tab Content
                        item {
                            BrandsTabContent(
                                brands = dashboardState.brands,
                                expandedBrands = dashboardState.expandedBrands,
                                isBrandLoading = dashboardState.isBrandLoading,
                                isBrandOperationLoading = dashboardState.isBrandOperationLoading,
                                onToggleAllBrands = { viewModel.toggleAllBrands() },
                                onToggleBrand = { brandId -> viewModel.toggleBrand(brandId) },
                                onDeleteBrand = { brandId -> viewModel.deleteBrand(brandId) },
                                onCreateSeries = { brandId, seriesName -> viewModel.createSeries(brandId, seriesName) },
                                onDeleteSeries = { brandId, seriesId -> viewModel.deleteSeries(brandId, seriesId) },
                                onCreateModel = { brandId, seriesId, modelName -> viewModel.createModel(brandId, seriesId, modelName) },
                                onDeleteModel = { brandId, seriesId, modelId -> viewModel.deleteModel(brandId, seriesId, modelId) },
                                onShowAddBrandDialog = { showAddBrandDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Brand Dialog
    if (showAddBrandDialog) {
        var brandName by remember { mutableStateOf(TextFieldValue()) }
        AlertDialog(
            onDismissRequest = { showAddBrandDialog = false },
            title = { Text("Yeni Marka Ekle") },
            text = {
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Marka Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (brandName.text.isNotBlank()) {
                            viewModel.createBrand(brandName.text)
                            showAddBrandDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBrandDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun StatsGrid(stats: GetStatsResponse?) {
    val statsItems = listOf(
        StatItem(
            title = "Toplam Kullanıcı",
            value = stats?.userCount?.toString() ?: "-",
            icon = Icons.Outlined.People,
            color = Color(0xFF6366F1) // Indigo
        ),
        StatItem(
            title = "Toplam İlan",
            value = stats?.listingCount?.toString() ?: "-",
            icon = Icons.Outlined.DirectionsCar,
            color = Color(0xFF10B981) // Emerald
        ),
        StatItem(
            title = "Toplam Favori",
            value = stats?.favoriteCount?.toString() ?: "-",
            icon = Icons.Outlined.Favorite,
            color = Color(0xFFF59E0B) // Amber
        ),
        StatItem(
            title = "Toplam Mesaj",
            value = stats?.messageCount?.toString() ?: "-",
            icon = Icons.Outlined.Chat,
            color = Color(0xFFEC4899) // Pink
        ),
        StatItem(
            title = "Toplam Bildirim",
            value = stats?.notificationCount?.toString() ?: "-",
            icon = Icons.Outlined.Notifications,
            color = Color(0xFF8B5CF6) // Purple
        ),
        StatItem(
            title = "Toplam Tahmin",
            value = stats?.predictCount?.toString() ?: "-",
            icon = Icons.Outlined.Analytics,
            color = Color(0xFFEF4444) // Red
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statsItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    StatCard(
                        statItem = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    statItem: StatItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = statItem.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = statItem.icon,
                contentDescription = null,
                tint = statItem.color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = statItem.value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = statItem.color
                )
                Text(
                    text = statItem.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserResponse,
    onDelete: () -> Unit,
    onUpdateRole: (String, Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(appRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = appRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                )
                Text(
                    text = "Son giriş: ${dateFormat.format(Date(user.lastLogin * 1000))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Role Badge with Click Handler
            Surface(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { showRoleDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = when (user.role) {
                    2L -> Color(0xFF6366F1) // Admin - Indigo
                    1L -> Color(0xFF10B981) // User - Emerald
                    else -> Color.Gray
                }.copy(alpha = 0.1f)
            ) {
                Text(
                    text = when (user.role) {
                        2L -> "Admin"
                        1L -> "Kullanıcı"
                        else -> "Misafir"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (user.role) {
                        2L -> Color(0xFF6366F1)
                        1L -> Color(0xFF10B981)
                        else -> Color.Gray
                    }
                )
            }

            // Delete Button
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kullanıcıyı Sil",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Delete User Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Kullanıcıyı Sil")
            },
            text = {
                Text("${user.firstName} ${user.lastName} kullanıcısını silmek istediğinizden emin misiniz?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Sil",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }

    // Role Change Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = {
                Text("Kullanıcı Rolünü Değiştir")
            },
            text = {
                Text("${user.firstName} ${user.lastName} kullanıcısının rolünü değiştirmek istediğinizden emin misiniz?")
            },
            confirmButton = {
                Column {
                    TextButton(
                        onClick = {
                            onUpdateRole(user.id, 1) // Normal User
                            showRoleDialog = false
                        }
                    ) {
                        Text(
                            "Normal Kullanıcı Yap",
                            color = Color(0xFF10B981)
                        )
                    }
                    TextButton(
                        onClick = {
                            onUpdateRole(user.id, 2) // Admin
                            showRoleDialog = false
                        }
                    ) {
                        Text(
                            "Admin Yap",
                            color = Color(0xFF6366F1)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRoleDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun BrandCard(
    brand: Brand,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteBrand: () -> Unit,
    onCreateSeries: (String) -> Unit,
    onDeleteSeries: (String) -> Unit,
    onCreateModel: (String, String) -> Unit,
    onDeleteModel: (String, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddSeriesDialog by remember { mutableStateOf(false) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var selectedSeriesId by remember { mutableStateOf<String?>(null) }
    var expandedSeries by remember { mutableStateOf<Set<String>>(emptySet()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Brand Image
                    if (brand.imagePath != null) {
                        AsyncImage(
                            model = brand.imagePath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = brand.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Row {
                    IconButton(
                        onClick = { showAddSeriesDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Seri Ekle",
                            tint = appRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Markayı Sil",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Series List
            if (isExpanded) {
                brand.series.forEach { series ->
                    SeriesItem(
                        series = series,
                        isExpanded = expandedSeries.contains(series.id),
                        onToggleExpand = {
                            expandedSeries = if (expandedSeries.contains(series.id)) {
                                expandedSeries - series.id
                            } else {
                                expandedSeries + series.id
                            }
                        },
                        onDelete = { onDeleteSeries(series.id) },
                        onAddModel = {
                            selectedSeriesId = series.id
                            showAddModelDialog = true
                        },
                        onDeleteModel = { modelId -> onDeleteModel(series.id, modelId) }
                    )
                }
            }
        }
    }

    // Delete Brand Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Markayı Sil") },
            text = { Text("${brand.name} markasını silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBrand()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sil", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Series Dialog
    if (showAddSeriesDialog) {
        var seriesName by remember { mutableStateOf(TextFieldValue()) }
        AlertDialog(
            onDismissRequest = { showAddSeriesDialog = false },
            title = { Text("Yeni Seri Ekle") },
            text = {
                OutlinedTextField(
                    value = seriesName,
                    onValueChange = { seriesName = it },
                    label = { Text("Seri Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (seriesName.text.isNotBlank()) {
                            onCreateSeries(seriesName.text)
                            showAddSeriesDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSeriesDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Model Dialog
    if (showAddModelDialog && selectedSeriesId != null) {
        var modelName by remember { mutableStateOf(TextFieldValue()) }
        AlertDialog(
            onDismissRequest = { showAddModelDialog = false },
            title = { Text("Yeni Model Ekle") },
            text = {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (modelName.text.isNotBlank() && selectedSeriesId != null) {
                            onCreateModel(selectedSeriesId!!, modelName.text)
                            showAddModelDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModelDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun SeriesItem(
    series: GetBrandSeries,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onAddModel: () -> Unit,
    onDeleteModel: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val models = series.models ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleExpand)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                if (models.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${models.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Row {
                IconButton(
                    onClick = onAddModel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Model Ekle",
                        tint = appRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Seriyi Sil",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Models List
        if (isExpanded) {
            if (models.isEmpty()) {
                Text(
                    text = "Bu seride henüz model bulunmuyor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                )
            } else {
                models.forEach { model ->
                    ModelItem(
                        model = model,
                        onDelete = { onDeleteModel(model.id) }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Seriyi Sil") },
            text = { 
                if (models.isNotEmpty()) {
                    Text("${series.name} serisini ve içindeki ${models.size} modeli silmek istediğinizden emin misiniz?")
                } else {
                    Text("${series.name} serisini silmek istediğinizden emin misiniz?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sil", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun ModelItem(
    model: GetBrandSeriesModels,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = model.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Modeli Sil",
                tint = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Modeli Sil") },
            text = { Text("${model.name} modelini silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sil", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

private data class StatItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun UsersTabContent(
    users: List<UserResponse>,
    isPaginating: Boolean,
    endReached: Boolean,
    onDeleteUser: (String) -> Unit,
    onLoadMore: () -> Unit,
    onUpdateRole: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Kullanıcılar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            users.forEach { user ->
                UserCard(
                    user = user,
                    onDelete = { onDeleteUser(user.id) },
                    onUpdateRole = { userId, role -> onUpdateRole(userId, role) }
                )
            }

            // Loading indicator for pagination
            if (isPaginating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            }

            // Load more trigger
            if (!endReached && !isPaginating) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun BrandsTabContent(
    brands: List<Brand>,
    expandedBrands: Set<String>,
    isBrandLoading: Boolean,
    isBrandOperationLoading: Boolean,
    onToggleAllBrands: () -> Unit,
    onToggleBrand: (String) -> Unit,
    onDeleteBrand: (String) -> Unit,
    onCreateSeries: (String, String) -> Unit,
    onDeleteSeries: (String, String) -> Unit,
    onCreateModel: (String, String, String) -> Unit,
    onDeleteModel: (String, String, String) -> Unit,
    onShowAddBrandDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAllBrands() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Markalar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expandedBrands.size == brands.size) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandedBrands.size == brands.size) "Daralt" else "Genişlet",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onShowAddBrandDialog,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Marka Ekle",
                    tint = appRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Brands Loading
            if (isBrandLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            }

            // Brands List
            brands.forEach { brand ->
                BrandCard(
                    brand = brand,
                    isExpanded = expandedBrands.contains(brand.id),
                    onToggleExpand = { onToggleBrand(brand.id) },
                    onDeleteBrand = { onDeleteBrand(brand.id) },
                    onCreateSeries = { seriesName -> onCreateSeries(brand.id, seriesName) },
                    onDeleteSeries = { seriesId -> onDeleteSeries(brand.id, seriesId) },
                    onCreateModel = { seriesId, modelName -> onCreateModel(brand.id, seriesId, modelName) },
                    onDeleteModel = { seriesId, modelId -> onDeleteModel(brand.id, seriesId, modelId) }
                )
            }

            // Brand Operation Loading
            if (isBrandOperationLoading) {
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
} 