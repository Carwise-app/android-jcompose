package com.carwise.android.view.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.R
import com.carwise.android.data.model.UserPayload
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.AuthViewModel
import com.carwise.android.viewmodel.home.HomeViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.text.NumberFormat
import java.util.*
import com.carwise.android.data.model.ListListingResponse
import com.carwise.android.data.model.Listing
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.carwise.android.view.components.ListingCard
import com.carwise.android.view.components.ShimmerListingCard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.TrendingDown
import com.carwise.android.data.model.Notification
import kotlin.math.abs
import androidx.compose.material.icons.outlined.Dashboard

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null,
    val category: DrawerCategory = DrawerCategory.MAIN
)

enum class DrawerCategory {
    MAIN,
    ACCOUNT,
    ADMIN
}

@Composable
private fun DrawerHeader(user: UserPayload?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(appRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = appRed
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column {
                if (user != null) {
                    Text(
                        text = "${user.first_name} ${user.last_name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Misafir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun PricePredictionCard(
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = appRed.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = appRed.copy(.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            ) {
                                append("Aracının Değerini\n")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = appRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("Carwise")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = appRed.copy(.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            ) {
                                append(" ile Keşfet")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            lineHeight = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Yapay zeka teknolojimiz ile aracının gerçek değerini saniyeler içinde öğren, doğru kararı ver.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp
                        ),
                        color = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hemen Keşfet",
                        style = MaterialTheme.typography.labelLarge,
                        color = appRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = appRed.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Değerini Öğren",
                            tint = appRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateListingCard(
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF2196F3).copy(.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            ) {
                                append("Aracını\n")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("Carwise")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF2196F3).copy(.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            ) {
                                append(" ile Sat")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            lineHeight = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Aracını güvenle sat, binlerce potansiyel alıcıya ulaş. Hemen ilanını oluştur ve satışa başla.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp
                        ),
                        color = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İlan Ver",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "İlan Ver",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingsHeader(
    total: Int,
    isLoading: Boolean,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Son İlanlar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = if (isLoading) "Yükleniyor..." else "Toplam $total ilan bulundu",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                textAlign = TextAlign.Start
            )
        }

        TextButton(onClick = onViewAllClick) {
            Text(
                text = "Tümünü Gör",
                color = appRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ListingItem(
    listing: Listing,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        ListingCard(
            listing = listing,
            onListingClick = onListingClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val homeState by homeViewModel.homeState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showNotifications by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = homeState.isListingsLoading)

    // Optimize scroll performance with derived state
    val appBarVisible by remember {
        derivedStateOf {
            val firstVisibleItem = listState.firstVisibleItemIndex
            val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset
            firstVisibleItem == 0 && firstVisibleItemOffset < 10
        }
    }

    // Optimize pagination with derived state
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            !homeState.isListingsLoading && !homeState.isPaginating && !homeState.endReached &&
            lastVisibleItem >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            homeViewModel.loadMoreListings()
        }
    }

    val drawerItems = remember(homeState.user) {
        listOf(
            // Ana Menü
            DrawerItem(
                title = "Ana Sayfa",
                icon = Icons.Default.Home,
                route = Screen.Home.route,
                category = DrawerCategory.MAIN
            ),
            DrawerItem(
                title = "Tüm İlanlar",
                icon = Icons.Default.List,
                route = Screen.Listings.route,
                category = DrawerCategory.MAIN
            ),
            DrawerItem(
                title = "İlanlarım",
                icon = Icons.Default.DirectionsCar,
                route = Screen.MyListings.route,
                category = DrawerCategory.MAIN
            ),
            DrawerItem(
                title = "Favorilerim",
                icon = Icons.Default.Favorite,
                route = Screen.Favorites.route,
                category = DrawerCategory.MAIN
            ),
            DrawerItem(
                title = "Mesajlarım",
                icon = Icons.Default.Chat,
                route = Screen.Chats.route,
                category = DrawerCategory.MAIN
            ),

            // Hesap İşlemleri
            DrawerItem(
                title = "Profilim",
                icon = Icons.Default.Person,
                route = Screen.Profile.route,
                category = DrawerCategory.ACCOUNT
            ),
            DrawerItem(
                title = "Ayarlar",
                icon = Icons.Default.Settings,
                route = Screen.Settings.route,
                category = DrawerCategory.ACCOUNT
            ),

            // Admin Menüsü (sadece admin kullanıcılar için)
            DrawerItem(
                title = "Dashboard",
                icon = Icons.Outlined.Dashboard,
                route = Screen.Dashboard.route,
                category = DrawerCategory.ADMIN
            )
        ).filter { item ->
            // Admin menüsünü sadece admin kullanıcılara göster
            item.category != DrawerCategory.ADMIN || homeState.user?.role == 2
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    // App Logo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Carwise Logo",
                            modifier = Modifier.size(64.dp),
                            colorFilter = ColorFilter.tint(appRed)
                        )
                        Text(
                            text = "Carwise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = appRed,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // User Header
                    DrawerHeader(user = homeState.user)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawer Items by Category
                    val mainItems = drawerItems.filter { it.category == DrawerCategory.MAIN }
                    val accountItems = drawerItems.filter { it.category == DrawerCategory.ACCOUNT }
                    val adminItems = drawerItems.filter { it.category == DrawerCategory.ADMIN }

                    // Ana Menü
                    if (mainItems.isNotEmpty()) {
                        DrawerCategoryHeader("Ana Menü")
                        mainItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        color = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                selected = item.route == navController.currentBackStackEntry?.destination?.route,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        item.route?.let { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = appRed.copy(alpha = 0.1f),
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    // Hesap İşlemleri
                    if (accountItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        DrawerCategoryHeader("Hesap İşlemleri")
                        accountItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        color = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                selected = item.route == navController.currentBackStackEntry?.destination?.route,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        item.route?.let { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = appRed.copy(alpha = 0.1f),
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    // Admin Menüsü
                    if (adminItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        DrawerCategoryHeader("Yönetici")
                        adminItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        color = if (item.route == navController.currentBackStackEntry?.destination?.route) appRed else Color.Gray
                                    )
                                },
                                selected = item.route == navController.currentBackStackEntry?.destination?.route,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        item.route?.let { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = appRed.copy(alpha = 0.1f),
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Çıkış Yap Butonu
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color.Red.copy(alpha = 0.7f)
                            )
                        },
                        label = {
                            Text(
                                text = "Çıkış Yap",
                                color = Color.Red.copy(alpha = 0.7f)
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                authViewModel.logout()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Red.copy(alpha = 0.1f),
                            unselectedContainerColor = Color.Transparent
                        )
                    )

                    // Bottom padding for better scrolling experience
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = appBarVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Carwise Logo",
                                    modifier = Modifier.size(50.dp),
                                    colorFilter = ColorFilter.tint(appRed)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch {
                                drawerState.open()
                            } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü",
                                    tint = appRed
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showNotifications = !showNotifications }) {
                                BadgedBox(
                                    badge = {
                                        if (homeState.unreadCount > 0) {
                                            Badge(
                                                containerColor = appRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = if (homeState.unreadCount > 99) "99+" else homeState.unreadCount.toString(),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Bildirimler",
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
                }
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { homeViewModel.refreshListings() }
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        // Optimize list performance
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Price Prediction Card
                        item(key = "price_prediction_card") {
                            PricePredictionCard(
                                onCardClick = { navController.navigate("price_prediction") }
                            )
                        }

                        // Create Listing Card
                        item(key = "create_listing_card") {
                            CreateListingCard(
                                onCardClick = { navController.navigate("create_listing") }
                            )
                        }

                        // Listings Header
                        item(key = "listings_header") {
                            ListingsHeader(
                                total = homeState.total,
                                isLoading = homeState.isListingsLoading,
                                onViewAllClick = { navController.navigate(Screen.Listings.route) }
                            )
                        }

                        // Listings
                        if (homeState.isListingsLoading) {
                            items(
                                items = List(8) { it },
                                key = { "shimmer_$it" }
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ShimmerListingCard()
                                }
                            }
                        } else if (homeState.user != null) {
                            items(
                                items = homeState.listings,
                                key = { it.id },
                                contentType = { "listing" }
                            ) { listing ->
                                ListingItem(
                                    listing = listing,
                                    onListingClick = { id ->
                                        navController.navigate(Screen.ListingDetail.createRoute(id))
                                    }
                                )
                            }

                            if (homeState.isPaginating) {
                                item(key = "loading_indicator") {
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

                // Notification dropdown
                if (showNotifications) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .width(350.dp)
                            .heightIn(max = 600.dp), // Maximum height
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Bildirimler",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                // Refresh button
                                IconButton(
                                    onClick = { homeViewModel.refreshNotifications() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yenile",
                                        tint = appRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show unread count
                            if (homeState.unreadCount > 0) {
                                Text(
                                    "${homeState.unreadCount} okunmamış bildirim",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appRed,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Error handling
                            homeState.notificationError?.let { error ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Red.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = error,
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { homeViewModel.clearNotificationError() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Kapat",
                                                tint = Color.Red,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Loading state
                            if (homeState.isNotificationsLoading && homeState.notifications.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = appRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            // Notifications list
                            else if (homeState.notifications.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 600.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(homeState.notifications) { notification ->
                                        NotificationItem(
                                            notification = notification,
                                            onRead = { homeViewModel.markNotificationAsRead(notification.id) },
                                            onDelete = { homeViewModel.deleteNotification(notification.id) },
                                            onNavigateToListing = {
                                                notification.data?.listing_id?.let{
                                                    navController.navigate(Screen.ListingDetail.createRoute(it))

                                                }
                                            },
                                            onNavigateToChat = {
                                                notification.data?.user_id?.let{
                                                    navController.navigate(Screen.Messages.createRoute(notification.data.listing_id ?: "", it))
                                                }
                                            }
                                        )
                                    }

                                    // Load more button
                                    if (!homeState.notificationEndReached) {
                                        item {
                                            if (homeState.isNotificationPaginating) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        color = appRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else {
                                                TextButton(
                                                    onClick = { homeViewModel.loadMoreNotifications() },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        "Daha fazla yükle",
                                                        color = appRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Empty state
                            else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsNone,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Henüz bildiriminiz bulunmuyor",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
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


@Composable
fun NotificationItem(
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToListing: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val statusColor = when (notification.status) {
        1L -> Color(0xFF6366F1) // SystemMessage - Indigo
        2L -> Color(0xFF10B981) // ChatMessage - Emerald
        3L -> Color(0xFFF59E0B) // FavoriteAdded - Amber
        4L -> Color(0xFFEF4444) // PriceDropped - Red
        else -> Color(0xFF6B7280) // Default - Gray
    }

    val statusIcon = when (notification.status) {
        1L -> Icons.Rounded.Info
        2L -> Icons.Rounded.Chat
        3L -> Icons.Rounded.Favorite
        4L -> Icons.Rounded.TrendingDown
        else -> Icons.Rounded.Notifications
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { showDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box {
            // Status indicator bar
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    statusColor,
                                    statusColor.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Status icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (!notification.read) FontWeight.SemiBold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time and status row
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTimeAgoDetailed(notification.createdAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            if (!notification.read) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = statusColor,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!notification.read) {
                            Surface(
                                onClick = onRead,
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Okundu işaretle",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = onDelete,
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Sil",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NotificationDetailDialog(
            notification = notification,
            onDismiss = { showDialog = false },
            onRead = {
                onRead()
                showDialog = false
            },
            onDelete = {
                onDelete()
                showDialog = false
            },
            onNavigateToListing = onNavigateToListing,
            onNavigateToChat = onNavigateToChat
        )
    }
}

@Composable
fun NotificationDetailDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToListing: () -> Unit = {},
    onNavigateToChat: () -> Unit
) {
    val statusColor = when (notification.status) {
        1L -> Color(0xFF6366F1) // SystemMessage - Indigo
        2L -> Color(0xFF10B981) // ChatMessage - Emerald
        3L -> Color(0xFFF59E0B) // FavoriteAdded - Amber
        4L -> Color(0xFFEF4444) // PriceDropped - Red
        else -> Color(0xFF6B7280) // Default - Gray
    }

    val statusIcon = when (notification.status) {
        1L -> Icons.Rounded.Info
        2L -> Icons.Rounded.Chat
        3L -> Icons.Rounded.Favorite
        4L -> Icons.Rounded.TrendingDown
        else -> Icons.Rounded.Notifications
    }

    val statusText = when (notification.status) {
        1L -> "Sistem Mesajı"
        2L -> "Sohbet Mesajı"
        3L -> "Favorilere Eklendi"
        4L -> "Fiyat Düştü"
        else -> "Bildirim"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                // Status header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTimeAgoDetailed(notification.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = statusColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                // Image gösterimi
                notification.data?.image?.let { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Bildirim görseli",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.placeholder),
                                error = painterResource(R.drawable.placeholder)
                            )
                        }
                    }
                }

               if (notification.status == 2L) {
                   notification.data?.listing_id?.let { listingId ->
                       if (listingId.isNotEmpty()) {
                           Spacer(modifier = Modifier.height(16.dp))

                           Card(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .clickable {
                                       onNavigateToChat()
                                       onDismiss()
                                   },
                               shape = RoundedCornerShape(12.dp),
                               colors = CardDefaults.cardColors(
                                   containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                               )
                           ) {
                               Row(
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .padding(16.dp),
                                   verticalAlignment = Alignment.CenterVertically,
                                   horizontalArrangement = Arrangement.SpaceBetween
                               ) {
                                   Row(
                                       verticalAlignment = Alignment.CenterVertically
                                   ) {
                                       Icon(
                                           imageVector = Icons.Rounded.AccountCircle,
                                           contentDescription = null,
                                           tint = MaterialTheme.colorScheme.primary,
                                           modifier = Modifier.size(24.dp)
                                       )

                                       Spacer(modifier = Modifier.width(12.dp))

                                       Column {
                                           notification.data?.user_name?.let {
                                               Text(
                                                   text = it,
                                                   style = MaterialTheme.typography.titleSmall,
                                                   color = MaterialTheme.colorScheme.primary,
                                                   fontWeight = FontWeight.SemiBold
                                               )
                                           }
                                           Text(
                                               text = "Detayları görmek için tıklayın",
                                               style = MaterialTheme.typography.bodySmall,
                                               color = MaterialTheme.colorScheme.onSurfaceVariant
                                           )
                                       }
                                   }

                                   Icon(
                                       imageVector = Icons.Rounded.ChevronRight,
                                       contentDescription = null,
                                       tint = MaterialTheme.colorScheme.primary,
                                       modifier = Modifier.size(20.dp)
                                   )
                               }
                           }
                       }
                   }
               } else {
                   notification.data?.listing_id?.let { listingId ->
                       if (listingId.isNotEmpty()) {
                           Spacer(modifier = Modifier.height(16.dp))

                           Card(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .clickable {
                                       onNavigateToListing()
                                       onDismiss()
                                   },
                               shape = RoundedCornerShape(12.dp),
                               colors = CardDefaults.cardColors(
                                   containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                               )
                           ) {
                               Row(
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .padding(16.dp),
                                   verticalAlignment = Alignment.CenterVertically,
                                   horizontalArrangement = Arrangement.SpaceBetween
                               ) {
                                   Row(
                                       verticalAlignment = Alignment.CenterVertically
                                   ) {
                                       Icon(
                                           imageVector = Icons.Rounded.ListAlt,
                                           contentDescription = null,
                                           tint = MaterialTheme.colorScheme.primary,
                                           modifier = Modifier.size(24.dp)
                                       )

                                       Spacer(modifier = Modifier.width(12.dp))

                                       Column {
                                           Text(
                                               text = "İlanı Görüntüle",
                                               style = MaterialTheme.typography.titleSmall,
                                               color = MaterialTheme.colorScheme.primary,
                                               fontWeight = FontWeight.SemiBold
                                           )
                                           Text(
                                               text = "Detayları görmek için tıklayın",
                                               style = MaterialTheme.typography.bodySmall,
                                               color = MaterialTheme.colorScheme.onSurfaceVariant
                                           )
                                       }
                                   }

                                   Icon(
                                       imageVector = Icons.Rounded.ChevronRight,
                                       contentDescription = null,
                                       tint = MaterialTheme.colorScheme.primary,
                                       modifier = Modifier.size(20.dp)
                                   )
                               }
                           }
                       }
                   }
               }

            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!notification.read) {
                    TextButton(
                        onClick = onRead,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Okundu")
                    }
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sil")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun formatTimeAgoDetailed(timestamp: Long): String {
    val currentTime = System.currentTimeMillis() / 1000
    val timeDifference = abs(currentTime - timestamp)

    return when {
        timeDifference < 30 -> "Şimdi"
        timeDifference < 60 -> "1 dakikadan az"
        timeDifference < 120 -> "1 dakika önce"
        timeDifference < 3600 -> "${timeDifference / 60} dakika önce"
        timeDifference < 7200 -> "1 saat önce"
        timeDifference < 86400 -> "${timeDifference / 3600} saat önce"
        timeDifference < 172800 -> "Dün"
        timeDifference < 604800 -> "${timeDifference / 86400} gün önce"
        timeDifference < 1209600 -> "1 hafta önce"
        timeDifference < 2592000 -> "${timeDifference / 604800} hafta önce"
        timeDifference < 5184000 -> "1 ay önce"
        timeDifference < 31536000 -> "${timeDifference / 2592000} ay önce"
        timeDifference < 63072000 -> "1 yıl önce"
        else -> "${timeDifference / 31536000} yıl önce"
    }
}