package com.carwise.android.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.ui.theme.appRed
import com.carwise.android.view.components.ListingCard
import com.carwise.android.viewmodel.profile.FavoritesViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorilerim",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = appRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = appRed
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error.toString(),
                            color = appRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.refreshFavorites() },
                            colors = ButtonDefaults.buttonColors(containerColor = appRed)
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                state.listings.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = androidx.compose.ui.graphics.Color.Gray
                        )
                        Text(
                            text = "Henüz favori ilanınız yok",
                            style = MaterialTheme.typography.titleMedium,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                }
                else -> {
                    val listState = rememberLazyListState()
                    val favorites = state.listings

                    // Pagination scroll tetikleyici
                    LaunchedEffect(listState, favorites.size, state.endReached, state.isPaginating) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val totalItemCount = layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            Pair(totalItemCount, lastVisibleItemIndex)
                        }.collectLatest { (totalItemCount, lastVisibleItemIndex) ->
                            if (totalItemCount > 0 && lastVisibleItemIndex >= totalItemCount - 6 &&
                                !state.endReached && !state.isPaginating
                            ) {
                                viewModel.loadMoreFavorites()
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = favorites,
                            key = { it.id }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onListingClick = { navController.navigate("listing_detail/${listing.id}") }
                            )
                        }
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
                }
            }
        }
    }
} 