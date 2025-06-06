package com.carwise.android.view.components

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.carwise.android.ui.theme.appRed
import com.carwise.android.view.listing.HtmlDisplayText
import java.text.NumberFormat
import java.util.*

@Composable
fun PreviewStep(
    title: String,
    price: Long?,
    currency: String,
    brand: String?,
    series: String?,
    model: String?,
    year: Int?,
    bodyType: String?,
    fuelType: String?,
    transmissionType: String?,
    driveType: String?,
    color: String?,
    enginePower: Long?,
    engineVolume: Long?,
    kilometers: Long?,
    city: String?,
    district: String?,
    neighborhood: String?,
    description: String,
    images: List<String>,
    isLoading: Boolean,
    onUpdateClick: () -> Unit,
    isUpdateScreen: Boolean = false
) {
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("tr", "TR")) }
    numberFormat.maximumFractionDigits = 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title and Price Section
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${numberFormat.format(price)} $currency",
                    style = MaterialTheme.typography.titleMedium,
                    color = appRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Vehicle Details Section
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
                    text = "Araç Detayları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DetailRow("Marka", brand ?: "-")
                DetailRow("Seri", series ?: "-")
                DetailRow("Model", model ?: "-")
                DetailRow("Yıl", year?.toString() ?: "-")
                DetailRow("Kasa Tipi", bodyType ?: "-")
                DetailRow("Yakıt Tipi", fuelType ?: "-")
                DetailRow("Vites Tipi", transmissionType ?: "-")
                DetailRow("Çekiş Tipi", driveType ?: "-")
                DetailRow("Renk", color ?: "-")
                DetailRow("Motor Gücü", "${enginePower ?: "-"} HP")
                DetailRow("Motor Hacmi", "${engineVolume ?: "-"} cc")
                DetailRow("Kilometre", "${numberFormat.format(kilometers ?: 0)} km")
            }
        }

        // Location Details Section
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
                    text = "Konum Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DetailRow("Şehir", city ?: "-")
                DetailRow("İlçe", district ?: "-")
                DetailRow("Mahalle", neighborhood ?: "-")
            }
        }

        // Description Section
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
                HtmlDisplayText(
                    html = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                )
            }
        }

    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
} 