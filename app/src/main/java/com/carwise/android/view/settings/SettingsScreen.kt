package com.carwise.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.components.LoadingDialog
import com.carwise.android.ui.components.SettingsCard
import com.carwise.android.ui.components.SettingsItem
import com.carwise.android.ui.components.SettingsDivider
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.shouldOpenSystemSettings) {
        if (state.shouldOpenSystemSettings) {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
            viewModel.resetSystemSettingsFlag()
        }
    }

    LaunchedEffect(state.cacheCleared) {
        if (state.cacheCleared) {
            snackbarMessage = "Önbellek başarıyla temizlendi"
            showSnackbar = true
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarMessage = it
            showSnackbar = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Ayarlar",
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bildirim Ayarları Kartı
                SettingsCard(
                    title = "Bildirim Ayarları",
                    titleColor = appRed
                ) {
                    SettingsItem(
                        title = "Uygulama Bildirimleri",
                        description = "Yeni ilanlar ve fiyat tahminleri hakkında bildirim alın",
                        icon = Icons.Default.Notifications,
                        iconTint = appRed,
                        isSwitchEnabled = state.appNotificationsEnabled,
                        showDivider = true,
                        onClick = { viewModel.updateAppNotifications(!state.appNotificationsEnabled) },
                        onSwitchChange = { viewModel.updateAppNotifications(it) }
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Email Bildirimleri",
                        description = "Önemli güncellemeler hakkında email alın",
                        icon = Icons.Default.Email,
                        iconTint = appRed,
                        isSwitchEnabled = state.emailNotificationsEnabled,
                        showDivider = false,
                        onClick = { viewModel.updateEmailNotifications(!state.emailNotificationsEnabled) },
                        onSwitchChange = { viewModel.updateEmailNotifications(it) }
                    )
                }

                // Gizlilik Ayarları Kartı
                SettingsCard(
                    title = "Gizlilik Ayarları",
                    titleColor = appRed
                ) {
                    SettingsItem(
                        title = "Veri Kullanımı",
                        description = "Önbellek ve veri yönetimi",
                        icon = Icons.Default.Storage,
                        iconTint = appRed,
                        onClick = { viewModel.showDataUsageDialog() }
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Gizlilik Politikası",
                        description = "Gizlilik politikamızı okuyun",
                        icon = Icons.Default.PrivacyTip,
                        iconTint = appRed,
                        onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "KVKK",
                        description = "Kişisel verilerin korunması hakkında bilgi",
                        icon = Icons.Default.Security,
                        iconTint = appRed,
                        onClick = { navController.navigate(Screen.KVKK.route) }
                    )
                }

                // Uygulama Bilgisi Kartı
                SettingsCard(
                    title = "Uygulama Bilgisi",
                    titleColor = appRed
                ) {
                    SettingsItem(
                        title = "Versiyon",
                        description = "",
                        icon = Icons.Default.Info,
                        iconTint = appRed,
                        showDivider = false
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Lisanslar",
                        description = "Kullanılan kütüphaneler ve lisanslar",
                        icon = Icons.Default.Description,
                        iconTint = appRed,
                        onClick = { navController.navigate(Screen.Licenses.route) }
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Hakkında",
                        description = "Carwise uygulaması hakkında bilgi",
                        icon = Icons.Default.Info,
                        iconTint = appRed,
                        onClick = { navController.navigate(Screen.About.route) }
                    )
                }
            }

            // Snackbar
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = appRed,
                    contentColor = Color.White,
                    action = {
                        TextButton(
                            onClick = { showSnackbar = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Tamam")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    }

    // Bildirim İzin Dialogu
    if (state.showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideNotificationDialog() },
            title = { 
                Text(
                    "Bildirim İzni",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = appRed
                    )
                ) 
            },
            text = { 
                Text(
                    "Bildirimleri alabilmek için lütfen bildirim iznini verin. Bu sayede yeni ilanlar ve fiyat tahminleri hakkında anında bilgilendirileceksiniz.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.proceedWithNotificationPermission() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = appRed
                    )
                ) {
                    Text("İzin Ver")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideNotificationDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = appRed
                    )
                ) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Veri Kullanımı Dialogu
    if (state.showDataUsageDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDataUsageDialog() },
            title = { 
                Text(
                    "Veri Kullanımı",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = appRed
                    )
                ) 
            },
            text = { 
                Text(
                    "Önbelleği temizlemek istediğinizden emin misiniz? Bu işlem geri alınamaz ve uygulama performansını etkileyebilir.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearCache()
                        viewModel.hideDataUsageDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = appRed
                    )
                ) {
                    Text("Temizle")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDataUsageDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = appRed
                    )
                ) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Yükleniyor Dialogu
    if (state.isLoading) {
        LoadingDialog()
    }
}

@Composable
private fun SettingsCard(
    title: String,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}
