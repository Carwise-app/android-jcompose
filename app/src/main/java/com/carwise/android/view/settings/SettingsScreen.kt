package com.carwise.android.view.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    // Sistem ayarlarına yönlendirme kontrolü
    LaunchedEffect(state.shouldOpenSystemSettings) {
        if (state.shouldOpenSystemSettings) {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            })
            viewModel.resetSystemSettingsFlag()
        }
    }

    // Bildirim izni dialogu
    if (state.showNotificationDialog) {
        Dialog(onDismissRequest = { viewModel.hideNotificationDialog() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bildirimleri Aç",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Carwise uygulamasından bildirim almak için bildirim izni vermeniz gerekmektedir. " +
                        "Bildirimler sayesinde ilanlar ve mesajlar hakkında anında haberdar olabilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.hideNotificationDialog() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Vazgeç")
                        }
                        
                        Button(
                            onClick = { viewModel.proceedWithNotificationPermission() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appRed
                            )
                        ) {
                            Text("Bildirimleri Aç")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", color = Color.Black) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Bildirim Ayarları Kartı
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
                            "Bildirim Ayarları",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Uygulama Bildirimleri
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Uygulama Bildirimleri",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                                Text(
                                    if (state.appNotificationsEnabled) 
                                        "Bildirimler açık" 
                                    else 
                                        "Bildirimleri açmak için tıklayın",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = appRed,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Switch(
                                    checked = state.appNotificationsEnabled,
                                    onCheckedChange = { viewModel.updateAppNotifications(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = appRed,
                                        checkedTrackColor = appRed.copy(alpha = 0.5f),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            color = Color.Gray.copy(alpha = 0.1f)
                        )

                        // Email Bildirimleri
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Email Bildirimleri",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                                Text(
                                    "Önemli güncellemeler için email alın",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = appRed,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Switch(
                                    checked = state.emailNotificationsEnabled,
                                    onCheckedChange = { viewModel.updateEmailNotifications(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = appRed,
                                        checkedTrackColor = appRed.copy(alpha = 0.5f),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Error Snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { /* TODO: Retry action */ }) {
                            Text("Tekrar Dene", color = Color.White)
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
} 