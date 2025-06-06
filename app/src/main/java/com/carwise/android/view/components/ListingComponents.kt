package com.carwise.android.view.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import coil3.request.CachePolicy

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
    isSelected: Boolean = false
) {
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onListingClick(listing.id) },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
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