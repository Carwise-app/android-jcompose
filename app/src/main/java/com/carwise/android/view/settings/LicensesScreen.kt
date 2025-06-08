package com.carwise.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carwise.android.ui.theme.appRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Lisanslar",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Android Jetpack
            LicenseCard(
                title = "Android Jetpack",
                description = "Android'in modern uygulama geliştirme bileşenleri",
                license = "Apache License 2.0",
                libraries = listOf(
                    "Compose UI",
                    "Compose Material3",
                    "Navigation Compose",
                    "Hilt Navigation Compose",
                    "Lifecycle ViewModel Compose",
                    "DataStore"
                )
            )

            // Kotlin
            LicenseCard(
                title = "Kotlin",
                description = "Modern programlama dili ve standart kütüphanesi",
                license = "Apache License 2.0",
                libraries = listOf(
                    "Kotlin Standard Library",
                    "Kotlin Coroutines",
                    "Kotlin Serialization"
                )
            )

            // Firebase
            LicenseCard(
                title = "Firebase",
                description = "Google'ın mobil ve web uygulamaları için platformu",
                license = "Apache License 2.0",
                libraries = listOf(
                    "Firebase Authentication",
                    "Firebase Firestore",
                    "Firebase Storage",
                    "Firebase Cloud Messaging"
                )
            )

            // Coil
            LicenseCard(
                title = "Coil",
                description = "Android için modern görüntü yükleme kütüphanesi",
                license = "Apache License 2.0",
                libraries = listOf(
                    "Coil Compose"
                )
            )

            // Retrofit
            LicenseCard(
                title = "Retrofit",
                description = "Android ve Java için tip güvenli HTTP istemcisi",
                license = "Apache License 2.0",
                libraries = listOf(
                    "Retrofit",
                    "OkHttp",
                    "Gson Converter"
                )
            )

            // Accompanist
            LicenseCard(
                title = "Accompanist",
                description = "Jetpack Compose için yardımcı kütüphaneler",
                license = "Apache License 2.0",
                libraries = listOf(
                    "System UI Controller",
                    "Permissions",
                    "Swipe Refresh"
                )
            )
        }
    }
}

@Composable
private fun LicenseCard(
    title: String,
    description: String,
    license: String,
    libraries: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = appRed
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Lisans: $license",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kullanılan Kütüphaneler:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            libraries.forEach { library ->
                Text(
                    text = "• $library",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
} 