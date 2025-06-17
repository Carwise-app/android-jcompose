package com.carwise.android.view.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.carwise.android.R
import com.carwise.android.data.model.Listing
import com.carwise.android.ui.theme.appRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil3.request.CachePolicy
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

enum class ListingCardType {
    COMPACT, // Type 1 - current implementation
    DETAILED // Type 2 - new detailed design
}

@Composable
private fun ListingImage(
    imageUrl: String,
    isSold: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(200, 140)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .placeholderMemoryCacheKey("placeholder")
            .build()
    }

    Box(
        modifier = modifier
            .size(100.dp, 70.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isSold) 0.4f else 1f),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            fallback = painterResource(R.drawable.placeholder)
        )
    }
}

@Composable
private fun ListingContent(
    title: String,
    city: String,
    district: String,
    price: String,
    isSold: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val titleColor = if (isSold) Color.Black.copy(.4f) else if (isSelected) Color.Black else Color(0xFF222222)
    val priceColor = if (isSold) appRed.copy(.4f) else appRed
    val locationColor = if (isSold) Color.Gray.copy(.4f) else Color.Gray

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = locationColor
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$city, $district",
                style = MaterialTheme.typography.bodySmall,
                color = locationColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = priceColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    cardType: ListingCardType = ListingCardType.COMPACT
) {
    when (cardType) {
        ListingCardType.COMPACT -> CompactListingCard(
            listing = listing,
            onListingClick = onListingClick,
            modifier = modifier,
            isSelected = isSelected
        )
        ListingCardType.DETAILED -> DetailedListingCard(
            listing = listing,
            onListingClick = onListingClick,
            modifier = modifier,
            isSelected = isSelected
        )
    }
}

@Composable
private fun CompactListingCard(
    listing: Listing,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val imageUrl = remember(listing.image.path) {
        "https://carwisegw.yusuftalhaklc.com${listing.image.path.removePrefix(".")}"
    }

    val formattedPrice = remember(listing.price, listing.currency) {
        val numberFormat = NumberFormat.getNumberInstance(Locale("tr", "TR"))
        numberFormat.maximumFractionDigits = 0
        "${numberFormat.format(listing.price)} ${listing.currency}"
    }

    val isSold = listing.status == 2
    val cardBg = if (isSelected) Color(0xFFE8F5E9) else Color.White
    
    // Combined scale animation for both press and bounce
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f // Scale down when pressed
            isExpanded -> 1.02f // Bounce when expanded
            else -> 1f // Normal size
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    // Expansion animation
    val expandedHeight by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(300),
        label = "expansion"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { 
                if (!isExpanded) onListingClick(listing.id)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isExpanded = !isExpanded
                    },
                    onTap = {
                        onListingClick(listing.id)
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 0.dp),
        shape = RoundedCornerShape(if (isExpanded) 8.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            if (isSold) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SATILDI",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListingImage(
                    imageUrl = imageUrl,
                    isSold = isSold
                )

                ListingContent(
                    title = listing.title,
                    city = listing.city,
                    district = listing.district,
                    price = formattedPrice,
                    isSold = isSold,
                    isSelected = isSelected,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp)
                )
            }

            // Expanded details section with animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    // Detail items in a row
                    val detailItems = listOf(
                        Triple(Icons.Default.DateRange, "Yıl", listing.year.toString()),
                        Triple(Icons.Default.Palette, "Renk", listing.color),
                        Triple(Icons.Default.Route, "KM", "${NumberFormat.getNumberInstance(Locale("tr", "TR")).format(listing.kilometers)}"),
                        Triple(Icons.Default.Speed, "Güç", "${listing.engine_power} HP"),
                        Triple(Icons.Default.Settings, "Motor", "${listing.engine_volume} cc"),
                        Triple(Icons.Default.Tune, "Vites", listing.transmission_type),
                        Triple(Icons.Default.LocalGasStation, "Yakıt", listing.fuel_type),
                        Triple(Icons.Default.DirectionsCar, "Kasa", listing.body_type),
                        Triple(Icons.Default.DriveEta, "Çekiş", listing.drive_type)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(detailItems) { item ->
                            ExpandedDetailItem(
                                icon = item.first,
                                title = item.second,
                                value = item.third
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedDetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .background(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = appRed,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF333333),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ShimmerListingCard() {
    val shimmerColors = remember {
        listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val xShimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = remember(xShimmer) {
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(xShimmer, 0f),
            end = Offset(xShimmer + 200f, 0f)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp) // 80 + 8 padding
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image shimmer
            Box(
                modifier = Modifier
                    .size(100.dp, 70.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Content shimmer
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedListingCard(
    listing: Listing,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val imageUrl = remember(listing.image.path) {
        "https://carwisegw.yusuftalhaklc.com${listing.image.path.removePrefix(".")}"
    }
    val isSold = listing.status == 2
    val formattedPrice = remember(listing.price, listing.currency) {
        val numberFormat = NumberFormat.getNumberInstance(Locale("tr", "TR"))
        numberFormat.maximumFractionDigits = 0
        "${numberFormat.format(listing.price)} ${listing.currency}"
    }
    val cardBg = if (isSelected) Color(0xFFFFF5F5) else Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onListingClick(listing.id) },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sol: Fotoğraf
            Box {
                ListingImage(
                    imageUrl = imageUrl,
                    isSold = isSold
                )
                if (isSold) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SATILDI",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sağ: Bilgi ve detaylar
            Column(
                modifier = Modifier
                    .height(70.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Üst: Başlık
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Altında: Marka, Seri, Model
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Text(
                        text = "${listing.brand.name} • ${listing.series.name} • ${listing.model.name}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Detaylar (ikon + value, 3 satır x 3 sütun)
            val detailItems = listOf(
                Triple(Icons.Default.DateRange, "Yıl", listing.year.toString()),
                Triple(Icons.Default.Palette, "Renk", listing.color),
                Triple(Icons.Default.Route, "KM", NumberFormat.getNumberInstance(Locale("tr", "TR")).format(listing.kilometers)),
                Triple(Icons.Default.Speed, "Güç", "${listing.engine_power} HP"),
                Triple(Icons.Default.Settings, "Motor", "${listing.engine_volume} cc"),
                Triple(Icons.Default.Tune, "Vites", listing.transmission_type),
                Triple(Icons.Default.LocalGasStation, "Yakıt", listing.fuel_type),
                Triple(Icons.Default.DirectionsCar, "Kasa", listing.body_type),
                Triple(Icons.Default.DriveEta, "Çekiş", listing.drive_type)
            )

            for (row in 0 until 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        if (index < detailItems.size) {
                            val item = detailItems[index]
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.first,
                                    contentDescription = item.second,
                                    tint = appRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.third,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Fiyat
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${listing.city}, ${listing.district}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = appRed,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DetailIconText(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Icon(icon, contentDescription = null, tint = appRed, modifier = Modifier.size(18.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp * 1000)
    val format = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("tr"))
    return format.format(date)
}