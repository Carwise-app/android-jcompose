package com.carwise.android.view.messages

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.core.view.WindowCompat
import com.carwise.android.data.model.Message
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.messages.MessagesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MessagesScreen(
    navController: NavController,
    listingId: String,
    userId: String,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var showNewMessageNotification by remember { mutableStateOf(false) }
    var lastMessageCount by remember { mutableStateOf(0) }

    // Mesajları ve grupları daha verimli şekilde hesapla
    val messageGroups = remember(messagesState.messages) {
        messagesState.messages
            .sortedByDescending { it.createdAt }
            .distinctBy { it.id }
            .let { messages ->
                messages.mapIndexed { index, message ->
                    MessageGroup(
                        message = message,
                        previousMessage = messages.getOrNull(index + 1),
                        nextMessage = messages.getOrNull(index - 1),
                        isFromMe = viewModel.isMessageFromCurrentUser(message)
                    )
                }
            }
    }

    // Load messages immediately
    LaunchedEffect(Unit) {
        viewModel.initialize(listingId, userId)
    }

    // Mesaj sayısı değiştiğinde scroll kontrolü
    LaunchedEffect(messagesState.messages.size) {
        if (messagesState.messages.isNotEmpty()) {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            val lastMessage = messagesState.messages.firstOrNull()
            val shouldAutoScroll = firstVisibleItemIndex <= 1 || 
                                 (lastMessage != null && viewModel.isMessageFromCurrentUser(lastMessage))

            if (shouldAutoScroll) {
                try {
                    listState.animateScrollToItem(0)
                    showNewMessageNotification = false
                } catch (e: Exception) {
                    listState.scrollToItem(0)
                    showNewMessageNotification = false
                }
            } else if (messagesState.messages.size > lastMessageCount) {
                // Yeni mesaj geldi ve kullanıcı en üstte değilse bildirim göster
                showNewMessageNotification = true
            }
            lastMessageCount = messagesState.messages.size
        }
    }

    // Pagination trigger - performans için optimize edildi
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            firstVisibleItemIndex
        }
            .distinctUntilChanged() // Sadece değişiklik olduğunda tetikle
            .collectLatest { firstVisibleItemIndex ->
                if (!messagesState.isLoading &&
                    !messagesState.isPaginating &&
                    !messagesState.endReached &&
                    firstVisibleItemIndex <= 5
                ) {
                    viewModel.loadMoreMessages(listingId, userId)
                }
            }
    }

    // System bar renklerini beyaz yap
    LaunchedEffect(Unit) {
        (context as? Activity)?.let { activity ->
            activity.window.apply {
                statusBarColor = android.graphics.Color.WHITE
                navigationBarColor = android.graphics.Color.WHITE
                WindowCompat.getInsetsController(this, decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // TopAppBar - sabit pozisyonda kalacak
            TopAppBar(
                title = {
                    if (messagesState.isLoading) {
                        // Shimmer for title while loading
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "${messagesState.chatUser?.firstName ?: ""} ${messagesState.chatUser?.lastName ?: ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            messagesState.listing?.let {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
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
                    if (messagesState.isLoading) {
                        // Shimmer for car icon while loading
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        )
                    } else {
                        IconButton(
                            onClick = { navController.navigate("listing_detail/${messagesState.listing?.id}") }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsCar,
                                contentDescription = "İlan Detayı",
                                tint = appRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )

            // Content area - TopAppBar ve BottomBar arasında kalan alan
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                if (messagesState.isLoading && messagesState.messages.isEmpty()) {
                    ShimmerLoading()
                } else if (messagesState.messages.isEmpty()) {
                    EmptyMessagesView()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        reverseLayout = true
                    ) {
                        items(
                            items = messageGroups,
                            key = { it.message.id }
                        ) { group ->
                            StableMessageItem(group = group)
                        }

                        if (messagesState.isPaginating) {
                            item(key = "loading_indicator") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = appRed,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BottomBar - klavye ile birlikte hareket edecek
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Mesajınızı yazın...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                coroutineScope.launch {
                                    viewModel.sendMessage(listingId, userId, messageText)
                                    messageText = ""
                                    keyboardController?.show()
                                    try {
                                        listState.animateScrollToItem(0)
                                    } catch (e: Exception) {
                                        listState.scrollToItem(0)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(appRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gönder",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Yeni mesaj bildirimi - klavye ile birlikte hareket edecek
        AnimatedVisibility(
            visible = showNewMessageNotification,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        coroutineScope.launch {
                            try {
                                listState.animateScrollToItem(0)
                                showNewMessageNotification = false
                            } catch (e: Exception) {
                                listState.scrollToItem(0)
                                showNewMessageNotification = false
                            }
                        }
                    },
                color = appRed.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "En üste git",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Yeni mesaj",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
private fun ShimmerLoading() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.7f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Messages shimmer
        repeat(8) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (it % 2 == 0) {
                    // Avatar shimmer for received messages
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                    
                    // Message bubble shimmer
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sender name shimmer
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        // Message bubble shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(brush)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    // Message bubble shimmer for sent messages
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMessagesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                color = Color.Gray
            )
        }
    }
}

// Mesaj grubu için data class
private data class MessageGroup(
    val message: Message,
    val previousMessage: Message?,
    val nextMessage: Message?,
    val isFromMe: Boolean
)

// Stable composable for message item
@Composable
private fun StableMessageItem(
    group: MessageGroup,
    modifier: Modifier = Modifier
) {
    val message = group.message
    val previousMessage = group.previousMessage
    val nextMessage = group.nextMessage
    val isFromMe = group.isFromMe

    // Mesajın grup içindeki pozisyonunu belirle - stable hesaplama
    val isFirstInGroup = remember(previousMessage, message) {
        previousMessage == null || 
        previousMessage.sender.id != message.sender.id ||
        message.createdAt - previousMessage.createdAt > 300
    }
    
    val isLastInGroup = remember(nextMessage, message) {
        nextMessage == null || 
        nextMessage.sender.id != message.sender.id ||
        nextMessage.createdAt - message.createdAt > 300
    }

    // Zaman formatını stable hale getir
    val formattedTime = remember(message.createdAt) {
        try {
            val messageTimeMillis = message.createdAt * 1000L
            val messageDate = Date(messageTimeMillis)
            val now = Date()
            val diffMillis = now.time - messageDate.time
            
            when {
                diffMillis < 60 * 1000 -> "Az önce"
                diffMillis < 60 * 60 * 1000 -> "${diffMillis / (60 * 1000)} dk önce"
                diffMillis < 24 * 60 * 60 * 1000 -> "${diffMillis / (60 * 60 * 1000)} sa önce"
                diffMillis < 7 * 24 * 60 * 1000 -> "${diffMillis / (24 * 60 * 60 * 1000)} gün önce"
                else -> {
                    val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale("tr"))
                    dateFormat.format(messageDate)
                }
            }
        } catch (e: Exception) {
            "Zaman bilgisi alınamadı"
        }
    }

    // Renkleri stable hale getir
    val bubbleColor = remember(isFromMe) { if (isFromMe) appRed else Color(0xFFF5F5F5) }
    val textColor = remember(isFromMe) { if (isFromMe) Color.White else Color.Black }
    val timeColor = remember(isFromMe) { if (isFromMe) Color.White.copy(alpha = 0.7f) else Color.Gray }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 4.dp,
                vertical = if (isFirstInGroup) 2.dp else 0.dp
            ),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 1.dp)
                .widthIn(max = 320.dp),
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
        ) {
            if (!isFromMe && isFirstInGroup) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(appRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${message.sender.firstName.firstOrNull()?.uppercase() ?: ""}${message.sender.lastName.firstOrNull()?.uppercase() ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = appRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else if (!isFromMe) {
                Spacer(modifier = Modifier.width(40.dp))
            }

            Column(
                horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
            ) {
                if (!isFromMe && isFirstInGroup) {
                    Text(
                        text = "${message.sender.firstName} ${message.sender.lastName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .widthIn(max = 320.dp),
                    colors = CardDefaults.cardColors(containerColor = bubbleColor),
                    shape = RoundedCornerShape(
                        topStart = if (isFirstInGroup) 16.dp else 4.dp,
                        topEnd = if (isFirstInGroup) 16.dp else 4.dp,
                        bottomStart = if (isLastInGroup) (if (isFromMe) 16.dp else 4.dp) else 4.dp,
                        bottomEnd = if (isLastInGroup) (if (isFromMe) 4.dp else 16.dp) else 4.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = if (isLastInGroup) 8.dp else 6.dp
                        )
                    ) {
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            modifier = Modifier.padding(bottom = if (isLastInGroup) 2.dp else 0.dp)
                        )
                        if (isLastInGroup) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = timeColor
                            )
                        }
                    }
                }
            }
        }
    }
} 