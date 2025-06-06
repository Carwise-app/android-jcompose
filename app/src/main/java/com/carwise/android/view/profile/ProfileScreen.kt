package com.carwise.android.view.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.carwise.android.R
import com.carwise.android.data.model.ProfileResponse
import com.carwise.android.data.model.UserPayload
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.view.auth.getCountries
import com.carwise.android.view.auth.getDefaultCountry
import com.carwise.android.viewmodel.AuthViewModel
import com.carwise.android.viewmodel.profile.ProfileViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val profileState by profileViewModel.profileState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = profileState.isLoading)



    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
    }

    // Handle successful profile update
    LaunchedEffect(profileState.isUpdateSuccess) {
        if (profileState.isUpdateSuccess) {
            showEditDialog = false
            profileViewModel.clearUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profilim",
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
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Profili Düzenle",
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
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { profileViewModel.refresh() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(appRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileState.profile?.image_url?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileState.profile?.image_url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profil Fotoğrafı",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                    tint = appRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Info
                        profileState.profile?.let { profile ->
                            Text(
                                text = "${profile.first_name} ${profile.last_name}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            if (profile.phone_number.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${profile.country_code} ${profile.phone_number}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Statistics Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "İlanlarım",
                        value = profileState.myListingsCount.toString(),
                        icon = Icons.Default.List,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.MyListings.route) }
                    )
                    StatCard(
                        title = "Favorilerim",
                        value = profileState.favoritesCount.toString(),
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Favorites.route) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                ActionButton(
                    title = "İlanlarımı Yönet",
                    icon = Icons.Default.List,
                    onClick = { navController.navigate(Screen.MyListings.route) }
                )
                ActionButton(
                    title = "Favorilerim",
                    icon = Icons.Default.Favorite,
                    onClick = { navController.navigate(Screen.Favorites.route) }
                )
                ActionButton(
                    title = "Ayarlar",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
                ActionButton(
                    title = "Çıkış Yap",
                    icon = Icons.Default.ExitToApp,
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Error Messages
        profileState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            profile = profileState.profile,
            isUpdating = profileState.isUpdating,
            error = profileState.updateError,
            onDismiss = { 
                if (!profileState.isUpdating) {
                    showEditDialog = false
                    profileViewModel.clearUpdateState()
                }
            },
            onUpdate = { firstName, lastName, countryCode, phoneNumber ->
                profileViewModel.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    countryCode = countryCode,
                    phoneNumber = phoneNumber
                )
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Çıkış Yap",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Çıkış yapmak istediğinizden emin misiniz?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    }
                ) {
                    Text(
                        text = "Evet",
                        color = appRed
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Hayır",
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Gray
        )
    }


}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) appRed else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) appRed else Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: ProfileResponse?,
    isUpdating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(profile?.first_name ?: "") }
    var lastName by remember { mutableStateOf(profile?.last_name ?: "") }
    var phoneNumber by remember { mutableStateOf(profile?.phone_number ?: "") }

    var selectedCountry by remember { mutableStateOf(getDefaultCountry()) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val context = LocalContext.current
    val phoneUtil = PhoneNumberUtil.createInstance(context)

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = {
            Text(
                text = "Profili Düzenle",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Ad") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        focusedLabelColor = appRed
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Soyad") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        focusedLabelColor = appRed
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showCountryPicker = true }
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 15.dp).height(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1A1A1A)
                        )
                        if (showCountryPicker) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Ülke Seç",
                                tint = appRed
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Ülke Seç",
                                tint = appRed
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        focusedLabelColor = appRed
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = appRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(firstName, lastName, selectedCountry.dialCode, phoneNumber)
                },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = appRed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Kaydet",
                        color = appRed
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text(
                    text = "İptal",
                    color = Color.Gray
                )
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,

            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Ülke Seçin",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Ülke Ara") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = appRed,
                        cursorColor = appRed
                    )
                )

                // Country List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val filteredCountries = getCountries(context,phoneUtil).filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.dialCode.contains(searchQuery)
                    }

                    items(filteredCountries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = country
                                    showCountryPicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flag,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = country.dialCode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            if (country.code == selectedCountry.code) {
                                Icon(
                                    imageVector = Icons.Filled.Circle,
                                    contentDescription = null,
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