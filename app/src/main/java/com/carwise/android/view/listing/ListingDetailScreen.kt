package com.carwise.android.view.listing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.listing.ListingDetailViewModel
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.carwise.android.data.model.UserPayload
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.unit.sp
import com.carwise.android.navigation.Screen
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.carwise.android.R
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.carwise.android.data.model.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImagePainter
import coil3.request.CachePolicy
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import coil3.request.bitmapConfig
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.text.Html
import android.widget.TextView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    navController: NavController,
    listingId: String?,
    viewModel: ListingDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        listingId?.let { viewModel.loadListingDetail(it) }
    }

    // Only navigate up if a listing was loaded before and now is deleted
    var wasListingNotNull by remember { mutableStateOf(false) }
    LaunchedEffect(state.listing) {
        if (state.listing != null) {
            wasListingNotNull = true
        }
        if (wasListingNotNull && state.listing == null && !state.isLoading && !state.isActionLoading) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "İlan Detayı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                actions = {
                    if (state.listing != null) {
                        // Favorite Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            enabled = !state.isFavoriteLoading
                        ) {
                            if (state.isFavoriteLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = appRed,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (state.listing!!.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = if (state.listing!!.isFavorite) "Favorilerden Çıkar" else "Favoriye Ekle",
                                    tint = appRed
                                )
                            }
                        }

                        // Share Button
                        IconButton(onClick = {
                            val url = "https://carwisegw.yusuftalhaklc.com/listing/${state.listing?.slug}"
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${state.listing?.title}\n\n$url")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "İlanı Paylaş"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş",
                                tint = appRed
                            )
                        }

                        // More Options Menu
                        if (state.currentUser != null && 
                            (state.currentUser!!.user_id == state.listing!!.createdBy.id || state.currentUser!!.role == 2)) {
                            var expanded by remember { mutableStateOf(false) }

                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Daha Fazla",
                                    tint = appRed
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                            ) {
                                // Edit Option (for both creator and admin)
                                DropdownMenuItem(
                                    text = { Text("Düzenle", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = appRed
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        navController.navigate(Screen.UpdateListing.createRoute(state.listing!!.id))
                                    },
                                    modifier = Modifier.background(Color.Transparent)
                                )

                                // Mark as Sold or Unsold Option (only for creator)
                                if (state.currentUser!!.user_id == state.listing!!.createdBy.id) {
                                    if (state.listing!!.status == 2) {
                                        DropdownMenuItem(
                                            text = { Text("Satışa Aç", color = appRed, fontWeight = FontWeight.Bold) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = appRed
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                viewModel.updateListingStatus(1) // Status 1 = Active
                                            },
                                            modifier = Modifier.background(Color.Transparent)
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Satıldı Olarak İşaretle", color = appRed, fontWeight = FontWeight.Bold) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = appRed
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                viewModel.updateListingStatus(2) // Status 2 = Sold
                                            },
                                            modifier = Modifier.background(Color.Transparent)
                                        )
                                    }
                                }

                                // Delete Option (for both creator and admin)
                                DropdownMenuItem(
                                    text = { Text("Kaldır", color = Color.Red, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.Red
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier.background(Color.Transparent)
                                )
                            }
                        }
                    }
                }
            )
        },
        containerColor = Color.White
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error.toString(),
                            color = appRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { listingId?.let { viewModel.loadListingDetail(it) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appRed
                            )
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                state.listing != null -> {
                    ListingDetailContent(
                        listing = state.listing!!,
                        navController = navController,
                        currentUser = state.currentUser
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "İlanı Kaldır",
                        style = MaterialTheme.typography.titleLarge,
                        color = appRed,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Bu ilanı kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            viewModel.deleteListing()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.ListingDetail.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = appRed)
                    ) {
                        Text("Kaldır", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("İptal", color = appRed)
                    }
                },
                containerColor = Color.White,
                titleContentColor = appRed,
                textContentColor = Color.Gray
            )
        }
    }
}

@Composable
private fun ListingDetailContent(
    listing: GetListingResponse,
    navController: NavController,
    currentUser: UserPayload?
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("tr", "TR")) }
    numberFormat.maximumFractionDigits = 0
    val formattedPrice = "${numberFormat.format(listing.price)} ${listing.currency}"
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Teknik Detaylar", "Açıklama")

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Title and Basic Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Image Gallery
        item {
            ListingImageGallery(
                images = listing.images,
                title = listing.title,
                isSold = listing.status == 2,
                onImageClick = { index ->
                    selectedImageIndex = index
                    showFullScreenImage = true
                }
            )
        }

        // Seller Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp,
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(appRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = appRed
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${listing.createdBy.firstName} ${listing.createdBy.lastName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${listing.createdBy.countryCode} ${listing.createdBy.phoneNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                            
                        if (currentUser != null && currentUser.user_id != listing.createdBy.id) {
                            IconButton(
                                onClick = {
                                    val userPhone = "${listing.createdBy.countryCode} ${listing.createdBy.phoneNumber}"
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$userPhone")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(appRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Ara",
                                    tint = appRed
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            IconButton(
                                onClick = { 
                                    navController.navigate("messages/${listing.id}/${listing.createdBy.id}")
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(appRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Message,
                                    contentDescription = "Mesaj Gönder",
                                    tint = appRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab View for Technical Details and Description
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    // Tab Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTabIndex = index }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) appRed else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            if (isSelected) appRed else Color.Transparent
                                        )
                                )
                            }
                        }
                    }

                    // Tab Content
                    when (selectedTabIndex) {
                        0 -> {
                            TechnicalDetailsTable(listing, numberFormat)
                            DamageDetailsSection(listing = listing)
                        }
                        1 -> DescriptionContent(listing.description)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Full Screen Image Dialog
    if (showFullScreenImage) {
        FullScreenImageViewer(
            images = listing.images,
            initialIndex = selectedImageIndex,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
private fun DescriptionContent(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        HtmlDisplayText(
            html = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
        )
    }
}

@Composable
fun htmlToAnnotatedString(html: String): AnnotatedString {
    val richTextState = rememberRichTextState()

    LaunchedEffect(html) {
        richTextState.setHtml(html)
    }

    return richTextState.annotatedString
}

// HTML içeriğini doğru formatlama ile gösteren komponenti
@Composable
fun HtmlDisplayText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Gray,
    lineHeight: TextUnit = style.lineHeight * 1.5f
) {
    val annotatedString = htmlToAnnotatedString(html)

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(
            lineHeight = lineHeight,
            color = color
        )
    )
}

@Composable
private fun PlaceholderImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(0.5f),
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun AsyncImageWithState(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    isThumbnail: Boolean = false,
    onLoading: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = appRed,
                modifier = Modifier.size(if (isThumbnail) 16.dp else 32.dp),
                strokeWidth = 1.dp
            )
        }
    },
    onError: @Composable () -> Unit = {
        PlaceholderImage(
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription
        )
    }
) {
    val context = LocalContext.current
    val imageSize = if (isThumbnail) 96 else 1080
    
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(imageSize)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .bitmapConfig(Bitmap.Config.RGB_565) // Daha az bellek kullanımı için
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .fallback(R.drawable.placeholder)
            .build()
    )

    val imageState = remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    
    LaunchedEffect(painter) {
        withContext(Dispatchers.IO) {
            painter.state.collect { state ->
                imageState.value = state
            }
        }
    }

    when (imageState.value) {
        is AsyncImagePainter.State.Loading -> onLoading()
        is AsyncImagePainter.State.Error -> onError()
        else -> {
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}

@Composable
private fun ThumbnailImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (isSelected) 
                    appRed.copy(alpha = 0.2f) 
                else 
                    Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) appRed else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImageWithState(
            imageUrl = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            isThumbnail = true
        )
    }
}

@Composable
private fun ListingImageGallery(
    images: List<Image>,
    title: String,
    isSold: Boolean,
    onImageClick: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                PlaceholderImage(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Fotoğraf bulunamadı"
                )
            }
        } else {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { images.size }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(page) }
                    ) {
                        val imageUrl = "https://carwisegw.yusuftalhaklc.com${images[page].path.removePrefix(".")}"
                        
                        AsyncImageWithState(
                            imageUrl = imageUrl,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        if (isSold) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = -25f },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SATILDI",
                                    color = appRed.copy(.5f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 70.sp,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Image Counter
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${images.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Thumbnail Preview - 2 rows with fixed size
            if (images.size > 1) {
                val itemsPerRow = 5
                val rows = (images.size + itemsPerRow - 1) / itemsPerRow
                val thumbnailSize = 64.dp
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(rows) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val startIndex = rowIndex * itemsPerRow
                            val endIndex = minOf(startIndex + itemsPerRow, images.size)
                            
                            for (index in startIndex until endIndex) {
                                val thumbnailUrl = "https://carwisegw.yusuftalhaklc.com${images[index].path.removePrefix(".")}"
                                ThumbnailImage(
                                    imageUrl = thumbnailUrl,
                                    modifier = Modifier
                                        .size(thumbnailSize)
                                        .padding(end = 4.dp),
                                    isSelected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(
    images: List<Image>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val pagerState = rememberPagerState(
                initialPage = initialIndex,
                pageCount = { images.size }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { _, pan, zoom, gestureRotation ->
                                    if (scale == 1f) {
                                        if (pan.x > 50f && page > 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(page - 1)
                                            }
                                        } else if (pan.x < -50f && page < images.size - 1) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(page + 1)
                                            }
                                        }
                                    } else {
                                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                                        rotation += gestureRotation
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                        rotation = 0f
                                    } else {
                                        scale = 2f
                                    }
                                }
                            )
                        }
                ) {
                    val imageUrl = "https://carwisegw.yusuftalhaklc.com${images[page].path.removePrefix(".")}"
                    
                    AsyncImageWithState(
                        imageUrl = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY,
                                rotationZ = rotation
                            ),
                        contentScale = ContentScale.Fit,
                        onLoading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )

                    // Zoom indicator
                    if (scale != 1f) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val radius = 100f
                            
                            rotate(rotation) {
                                scale(scale) {
                                    translate(offsetX, offsetY) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.3f),
                                            radius = radius,
                                            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                                            style = Stroke(
                                                width = 1f,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.White
                )
            }

            // Image Counter
            if (images.size > 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${images.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Zoom Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        scale = (scale * 1.5f).coerceIn(0.5f, 5f)
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Yakınlaştır",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        scale = (scale / 1.5f).coerceIn(0.5f, 5f)
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Uzaklaştır",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        rotation = (rotation + 90f) % 360f
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Döndür",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        rotation = 0f
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sıfırla",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp),
        color = appRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = appRed
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = appRed
            )
        }
    }
}

@Composable
private fun TechnicalDetailsTable(listing: GetListingResponse, numberFormat: NumberFormat) {
    val detail = listing.detail
    val rows = listOf(
        "Konum" to "${listing.city}, ${listing.district}, ${listing.neighborhood}",
        "Fiyat" to "${numberFormat.format(listing.price)} ${listing.currency}",
        "İlan Tarihi" to SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(listing.createdAt * 1000L)),
        "Marka" to listing.brand.name,
        "Seri" to listing.series.name,
        "Model" to listing.model.name,
        "Yıl" to detail.year,
        "Renk" to detail.color,
        "Yakıt Tipi" to detail.fuelType,
        "Vites" to detail.transmissionType,
        "KM" to numberFormat.format(detail.kilometers),
        "Kasa Tipi" to detail.bodyType,
        "Motor Gücü" to "${detail.enginePower} hp",
        "Motor Hacmi" to "${detail.engineVolume} cc",
        "Çekiş" to detail.driveType,
        "Ağır Hasar Kayıtlı" to if (detail.heavyDamage) "Evet" else "Hayır",
    )
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            rows.forEachIndexed { idx, (label, value) ->
                val modifier = if (idx == 0) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable {
                            val locationQuery = "${listing.city}, ${listing.district}, ${listing.neighborhood}"
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(locationQuery)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://maps.google.com/maps?q=${Uri.encode(locationQuery)}")
                                )
                                context.startActivity(browserIntent)
                            }
                        }
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                }

                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (idx == 1) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appRed,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
                if (idx < rows.lastIndex) Divider(color = Color(0xFFE0E0E0), thickness = 0.7.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DamageDetailsSection(listing: GetListingResponse) {
    val detail = listing.detail
    val parts = listOf(
        "Ön Tampon" to detail.frontBumper,
        "Ön Kaput" to detail.frontHood,
        "Tavan" to detail.roof,
        "Ön Sağ Kapı" to detail.frontRightDoor,
        "Arka Sağ Kapı" to detail.rearRightDoor,
        "Ön Sol Çamurluk" to detail.frontLeftMudguard,
        "Ön Sol Kapı" to detail.frontLeftDoor,
        "Arka Sol Kapı" to detail.rearLeftDoor,
        "Arka Sol Çamurluk" to detail.rearLeftMudguard,
        "Arka Tampon" to detail.rearBumper
    )

    val statusOrder = listOf("orijinal", "boyalı", "lokal", "değişmiş", "belirtilmemiş")
    val statusColors = mapOf(
        "orijinal" to Color(0xFF4CAF50),
        "boyalı" to Color(0xFFFFC107),
        "lokal" to Color(0xFFFFF176),
        "değişmiş" to Color(0xFFF44336),
        "belirtilmemiş" to Color(0xFFBDBDBD)
    )
    val statusLabels = mapOf(
        "orijinal" to "Orijinal",
        "boyalı" to "Boyalı",
        "lokal" to "Lokal Boyalı",
        "değişmiş" to "Değişmiş",
        "belirtilmemiş" to "Belirsiz"
    )

    val grouped = parts.groupBy {
        when (it.second.lowercase()) {
            "orijinal", "yok" -> "orijinal"
            "boyalı" -> "boyalı"
            "lokal", "lokal boya" -> "lokal"
            "değişmiş" -> "değişmiş"
            "", "belirtilmemiş" -> "belirtilmemiş"
            else -> it.second.lowercase()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Boya, Değişen ve Tramer",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            Divider(color = Color.LightGray, thickness = 0.7.dp)

            statusOrder.forEach { status ->
                val items = grouped[status] ?: emptyList()
                if (items.isNotEmpty()) {
                    LegendDotDot(statusColors[status] ?: Color.Gray, statusLabels[status] ?: status.capitalize())
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 90.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = ((items.size / 2 + 1) * 40).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        userScrollEnabled = false
                    ) {
                        items(items) { (part, _) ->
                            val badgeTextColor = if (status == "lokal") Color.DarkGray else statusColors[status] ?: Color.Gray
                            DamageBadge(part, statusColors[status] ?: Color.Gray, badgeTextColor)
                        }
                    }
                }
            }

            if (statusOrder.all { (grouped[it]?.isEmpty() ?: true) }) {
                Text(
                    text = "Hasar detayı belirtilmemiş.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 0.7.dp, modifier = Modifier.padding(vertical = 6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                statusOrder.forEach { status ->
                    LegendDotDot(statusColors[status] ?: Color.Gray, statusLabels[status] ?: status.capitalize())
                }
            }
        }
    }
}

@Composable
private fun DamageBadge(label: String, color: Color, textColor: Color = color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .padding(vertical = 1.dp)
            .height(32.dp)
            .wrapContentWidth()
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LegendDotDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
} 