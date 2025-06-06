package com.carwise.android.view.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.carwise.android.data.model.Chat
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.chats.ChatsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.text.NumberFormat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import coil3.request.CachePolicy
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
private fun ChatItemImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(140, 140) // 2x for better quality
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .placeholderMemoryCacheKey("chat_placeholder")
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ChatItemContent(
    chat: Chat,
    userInitials: String,
    formattedTime: String,
    formattedPrice: String,
    location: String,
    onClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable { onClick(chat.listing.id, chat.user.id) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with Initials
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(appRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    style = MaterialTheme.typography.titleMedium,
                    color = appRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Content Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // User name and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chat.user.firstName} ${chat.user.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Car model info
                Text(
                    text = "${chat.listing.brand.name} ${chat.listing.series.name} ${chat.listing.model.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Car image
            ChatItemImage(
                imageUrl = "https://carwisegw.yusuftalhaklc.com${chat.listing.image.path.removePrefix(".")}",
                modifier = Modifier.size(70.dp)
            )
        }
    }
}

@Composable
private fun EmptyChatsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Henüz mesajınız bulunmuyor",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatItem(
    chat: Chat,
    onClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userInitials = remember(chat.user.firstName, chat.user.lastName) {
        val firstInitial = chat.user.firstName.firstOrNull()?.uppercase() ?: ""
        val lastInitial = chat.user.lastName.firstOrNull()?.uppercase() ?: ""
        "$firstInitial$lastInitial"
    }

    val formattedTime = remember(chat.lastMessageTime) {
        val now = System.currentTimeMillis() / 1000
        val diff = now - chat.lastMessageTime
        when {
            diff < 60 -> "Az önce"
            diff < 3600 -> "${diff / 60} dk önce"
            diff < 86400 -> "${diff / 3600} sa önce"
            diff < 604800 -> "${diff / 86400} gün önce"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale("tr"))
                dateFormat.format(Date(chat.lastMessageTime * 1000L))
            }
        }
    }

    ChatItemContent(
        chat = chat,
        userInitials = userInitials,
        formattedTime = formattedTime,
        formattedPrice = "", // Not used anymore
        location = "", // Not used anymore
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun ChatItemWithDivider(
    chat: Chat,
    onClick: (String, String) -> Unit,
    isLastItem: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ChatItem(
            chat = chat,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
        if (!isLastItem) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.8f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.8f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim.value, y = translateAnim.value),
        tileMode = TileMode.Clamp
    )

    Box(
        modifier = modifier
            .background(brush)
    )
}

@Composable
private fun ChatItemShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar shimmer
            ShimmerEffect(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Content shimmer
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name shimmer
                ShimmerEffect(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Car model shimmer
                ShimmerEffect(
                    modifier = Modifier
                        .width(180.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Image shimmer
            ShimmerEffect(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun ShimmerLoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        repeat(8) { index ->
            ChatItemShimmer()
            if (index < 7) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavController,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val chatsState by viewModel.chatsState.collectAsState()
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = chatsState.isRefreshing)
    val coroutineScope = rememberCoroutineScope()

    // Remember the last item index for divider logic
    val lastItemIndex = remember(chatsState.chats.size) {
        chatsState.chats.size - 1
    }

    // Optimize pagination with derived state
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = chatsState.chats.size
            !chatsState.isLoading && 
            !chatsState.isPaginating && 
            !chatsState.endReached && 
            lastVisibleItem >= totalItems - 3
        }
    }

    // Handle pagination
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadOlderChats()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mesajlarım",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when {
                chatsState.isLoading && chatsState.chats.isEmpty() -> {
                    ShimmerLoadingContent()
                }
                chatsState.chats.isEmpty() -> {
                    EmptyChatsContent()
                }
                else -> {
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { viewModel.refreshChats() }
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            itemsIndexed(
                                items = chatsState.chats,
                                key = { _, chat -> "${chat.listing.id}_${chat.user.id}" }
                            ) { index, chat ->
                                ChatItemWithDivider(
                                    chat = chat,
                                    onClick = { listingId, userId ->
                                        coroutineScope.launch {
                                            navController.navigate(Screen.Messages.createRoute(listingId, userId))
                                        }
                                    },
                                    isLastItem = index == lastItemIndex
                                )
                            }

                            if (chatsState.isPaginating) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = appRed,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 