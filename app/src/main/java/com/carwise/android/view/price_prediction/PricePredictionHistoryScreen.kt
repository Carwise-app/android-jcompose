package com.carwise.android.view.price_prediction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.price_prediction.PricePredictionHistoryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricePredictionHistoryScreen(
    navController: NavController,
    viewModel: PricePredictionHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Sayfalama için scroll listener
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= state.predictions.size - 5) {
                    viewModel.loadMorePredictions()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tahmin Geçmişi", color = Color.Black) },
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
            if (state.isLoading && state.predictions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appRed)
                }
            } else if (state.predictions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz tahmin geçmişi bulunmuyor",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.predictions) { prediction ->
                        var expanded by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Temel Bilgiler
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${prediction.brand} ${prediction.series} ${prediction.model}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "₺${String.format("%,d", prediction.price.toLong())}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = appRed
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${prediction.year}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "${String.format("%,d", prediction.mileage)} km",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = prediction.transmissionType,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (expanded) "Daralt" else "Genişlet",
                                        tint = Color.Gray
                                    )
                                }

                                // MAE Gösterimi
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = appRed.copy(alpha = 0.1f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Tahmin Sapması: ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appRed
                                        )
                                        Text(
                                            text = "₺${String.format("%,d", prediction.mae.toLong())}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appRed,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }

                                // Detaylı Bilgiler
                                AnimatedVisibility(
                                    visible = expanded,
                                    enter = expandVertically(animationSpec = tween(300)),
                                    exit = shrinkVertically(animationSpec = tween(300))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Divider(color = Color.LightGray)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        DetailRow("Yakıt", prediction.fuelType)
                                        DetailRow("Kasa", prediction.bodyType)
                                        DetailRow("Renk", prediction.color)
                                        DetailRow("Motor Hacmi", prediction.engineVolume.toString())
                                        DetailRow("Motor Gücü", prediction.enginePower.toString())
                                        DetailRow("Hasar", prediction.accidentHistory.toString())
                                        DetailRow("Orijinal Parça",
                                            prediction.originalParts.toString()
                                        )
                                        DetailRow("Değişen Parça",
                                            prediction.replacedParts.toString()
                                        )
                                        DetailRow("Boyalı Parça",
                                            prediction.paintedParts.toString()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Loading indicator for pagination
                    if (state.isLoading && state.predictions.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = appRed
                                )
                            }
                        }
                    }
                }
            }

            // Error Message
            state.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
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
            color = Color.Black
        )
    }
} 