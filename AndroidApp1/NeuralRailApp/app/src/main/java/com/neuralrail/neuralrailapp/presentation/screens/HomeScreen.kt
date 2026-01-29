package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.EcoCommuteData
import com.neuralrail.neuralrailapp.data.remote.UserDto
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.EcoCommuteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

data class SearchResult(val title: String, val subtitle: String, val route: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: EcoCommuteViewModel, currentUser: UserDto? = null, onNavigate: (String) -> Unit = {}, onLogout: () -> Unit = {}) {
    val state by viewModel.ecoCommuteState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }
    var isScrolled by remember { mutableStateOf(false) }
    
    val trainStatusTitle = stringResource(R.string.train_status)
    val trainStatusDesc = stringResource(R.string.train_status_desc)
    val smartPlannerTitle = stringResource(R.string.smart_planner)
    val smartPlannerDesc = stringResource(R.string.smart_planner_desc)
    val liveEnergyTitle = stringResource(R.string.live_energy)
    val liveEnergyDesc = stringResource(R.string.live_energy_desc)
    val carbonOffsetTitle = stringResource(R.string.support)
    val carbonOffsetDesc = stringResource(R.string.carbon_offset_desc)
    val educationHubTitle = stringResource(R.string.education_hub)
    val educationHubDesc = stringResource(R.string.education_hub_desc)
    val communityWatchTitle = stringResource(R.string.report_problem)
    val communityWatchDesc = stringResource(R.string.community_watch_desc)
    val greenChallengesTitle = stringResource(R.string.green_challenges)
    val greenChallengesDesc = stringResource(R.string.challenges_desc)
    val qrScannerTitle = stringResource(R.string.qr_scanner)
    val qrScannerDesc = stringResource(R.string.qr_scanner_desc)
    
    val allFeatures = listOf(
        SearchResult(trainStatusTitle, trainStatusDesc, "train_status", Icons.Default.Train, AccentCyan),
        SearchResult(smartPlannerTitle, smartPlannerDesc, "planner", Icons.Default.Map, BlueAccent),
        SearchResult(liveEnergyTitle, liveEnergyDesc, "green_rail", Icons.Default.Bolt, AccentCyan),
        SearchResult(carbonOffsetTitle, carbonOffsetDesc, "carbon_offset", Icons.Default.Eco, AccentGreen),
        SearchResult(educationHubTitle, educationHubDesc, "education_hub", Icons.Default.School, AccentYellow),
        SearchResult(communityWatchTitle, communityWatchDesc, "community_watch", Icons.Default.Visibility, AccentOrange),
        SearchResult(greenChallengesTitle, greenChallengesDesc, "challenges", Icons.Default.EmojiEvents, AccentYellow),
        SearchResult(qrScannerTitle, qrScannerDesc, "qr_scanner", Icons.Default.QrCodeScanner, BlueAccent)
    )
    
    val searchResults = if (searchQuery.isNotEmpty()) {
        allFeatures.filter { it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    // Get theme-aware colors
    val appColors = com.neuralrail.neuralrailapp.presentation.theme.LocalAppColors.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
            // Header always visible
            AppHeader(onUserClick = { showDrawer = true }, isCollapsed = false)
            
            // Search bar always visible - use key to maintain state
            SearchBarSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                isCollapsed = false,
                appColors = appColors
            )
            
            // Content changes based on search state
            if (searchQuery.isNotEmpty()) {
                SearchResultsContent(
                    searchResults, 
                    onNavigate = { route -> searchQuery = ""; onNavigate(route) }
                ) { searchQuery = "" }
            } else {
                when (val currentState = state) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) }
                    }
                    is UiState.Success -> HomeContentOnly(
                        data = currentState.data,
                        onNavigate = onNavigate
                    )
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(currentState.message, color = appColors.textSecondary) }
                    }
                }
            }
        }
        
        // Animated Drawer - optimized transitions
        AnimatedVisibility(
            visible = showDrawer,
            enter = fadeIn(tween(180, easing = LinearEasing)),
            exit = fadeOut(tween(150, easing = LinearEasing))
        ) {
            UserDrawer(
                user = currentUser,
                onDismiss = { showDrawer = false },
                onNavigate = { route -> showDrawer = false; onNavigate(route) },
                onLogout = { showDrawer = false; onLogout() }
            )
        }
    }
}


@Composable
private fun UserDrawer(user: UserDto?, onDismiss: () -> Unit, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val drawerWidth = 300.dp
    val density = LocalDensity.current
    val drawerWidthPx = with(density) { drawerWidth.toPx() }
    
    // Single animation progress for smoother performance
    var isContentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isContentVisible = true }
    
    // Use single Animatable for better performance
    val animationProgress by animateFloatAsState(
        targetValue = if (isContentVisible) 1f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "drawer_progress"
    )
    
    // Derive all values from single progress
    val slideOffset = drawerWidthPx * (1f - animationProgress)
    val scaleX = 0.4f + (0.6f * animationProgress)
    val scaleY = 0.2f + (0.8f * animationProgress)
    val backgroundAlpha = 0.6f * animationProgress
    val contentAlpha = animationProgress
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim background with tap to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backgroundAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
        
        // Drawer sliding from RIGHT with expand animation
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .align(Alignment.CenterEnd)
                .offset { IntOffset(slideOffset.roundToInt(), 0) }
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0f) // Top-right origin
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount > 20) onDismiss()
                        }
                    )
                },
            color = LocalAppColors.current.backgroundCard,
            shadowElevation = 24.dp,
            shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha }
            ) {
                // Animated Header
                DrawerHeader(user)
                
                Spacer(Modifier.height(16.dp))
                
                // Menu Items with staggered animation
                val myProfileText = stringResource(R.string.my_profile)
                val settingsText = stringResource(R.string.settings)
                val aboutText = stringResource(R.string.about)
                val menuItems = listOf(
                    Triple(Icons.Default.Person, myProfileText, "profile"),
                    Triple(Icons.Default.Settings, settingsText, "settings"),
                    Triple(Icons.Default.Info, aboutText, "about")
                )
                val colors = listOf(AccentCyan, BlueAccent, AccentGreen)
                
                menuItems.forEachIndexed { index, (icon, title, route) ->
                    AnimatedDrawerItem(
                        icon = icon,
                        title = title,
                        color = colors[index],
                        delay = 100 + index * 60,
                        onClick = { onNavigate(route) }
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
                
                Spacer(Modifier.height(8.dp))
                
                // Logout with special styling
                val logOutText = stringResource(R.string.log_out)
                AnimatedDrawerItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = logOutText,
                    color = AccentRed,
                    delay = 280,
                    isLogout = true,
                    onClick = onLogout
                )
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DrawerHeader(user: UserDto?) {
    // Removed infinite transition for better performance - static gradient looks cleaner
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(BluePrimaryDark, BluePrimary, BlueSecondary)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // Simplified avatar animation
            var avatarScale by remember { mutableFloatStateOf(0f) }
            val animatedScale by animateFloatAsState(
                targetValue = avatarScale,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "avatar_scale"
            )
            LaunchedEffect(Unit) { avatarScale = 1f }
            
            Surface(
                modifier = Modifier.size(72.dp).scale(animatedScale),
                shape = CircleShape,
                color = AccentCyan.copy(alpha = 0.3f),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            val guestUserText = stringResource(R.string.guest_user)
            Text(
                user?.full_name ?: guestUserText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                user?.email ?: "guest@neuralrail.com",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Stats row
            val daysText = stringResource(R.string.days)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DrawerStatChip(Icons.Default.Eco, "${user?.total_co2_saved?.toInt() ?: 0} ${stringResource(R.string.kg)}", AccentGreen)
                DrawerStatChip(Icons.Default.LocalFireDepartment, "${user?.streak_days ?: 0} $daysText", AccentOrange)
            }
        }
    }
}

@Composable
private fun DrawerStatChip(icon: ImageVector, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AnimatedDrawerItem(
    icon: ImageVector,
    title: String,
    color: Color,
    delay: Int,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }
    
    // Single animation for smoother performance
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "item_progress"
    )
    
    val offsetX = (80 * (1f - progress)).toInt()
    val alpha = progress
    val itemScale = 0.8f + (0.2f * progress)
    
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset { IntOffset(offsetX, 0) }
            .graphicsLayer { 
                this.alpha = alpha
                this.scaleX = itemScale * pressScale
                this.scaleY = itemScale * pressScale
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        color = if (isLogout) color.copy(alpha = 0.12f) else LocalAppColors.current.backgroundCard.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = if (isLogout) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isLogout) color else LocalAppColors.current.textPrimary
            )
            Spacer(Modifier.weight(1f))
            if (!isLogout) {
                Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}



@Composable
private fun SearchResultsContent(results: List<SearchResult>, onNavigate: (String) -> Unit, onClear: () -> Unit) {
    val searchResultsText = stringResource(R.string.search_results)
    val clearText = stringResource(R.string.clear)
    val noResultsText = stringResource(R.string.no_results_found)
    val tryDifferentText = stringResource(R.string.try_different_search)
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$searchResultsText (${results.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = LocalAppColors.current.textPrimary)
                TextButton(onClick = onClear) { Text(clearText, color = BlueAccent) }
            }
        }
        if (results.isEmpty()) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = LocalAppColors.current.backgroundCard) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, tint = LocalAppColors.current.textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(noResultsText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LocalAppColors.current.textPrimary)
                        Text(tryDifferentText, fontSize = 14.sp, color = LocalAppColors.current.textSecondary)
                    }
                }
            }
        } else {
            items(results) { result ->
                Surface(modifier = Modifier.fillMaxWidth().clickable { onNavigate(result.route) }, shape = RoundedCornerShape(12.dp), color = LocalAppColors.current.backgroundCard) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = result.color.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(result.icon, null, tint = result.color, modifier = Modifier.size(26.dp)) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(result.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LocalAppColors.current.textPrimary)
                            Text(result.subtitle, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsingHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onUserClick: () -> Unit,
    isCollapsed: Boolean
) {
    // Use spring animation for smoother, more natural feel
    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f
        ),
        label = "collapse"
    )
    
    // Derive all values from single progress for 60fps smoothness
    val expandedProgress = 1f - collapseProgress
    
    // Gradient background for modern look
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BluePrimaryDark,
                        BluePrimary,
                        BluePrimary.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = (12 + (4 * expandedProgress)).dp)
        ) {
            // Top row - Modern header design
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Logo and branding
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Animated logo container with glow effect
                    Box(contentAlignment = Alignment.Center) {
                        // Glow effect behind logo
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = AccentCyan.copy(alpha = 0.2f)
                        ) {}
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            AccentCyan.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            ) {
                                Icon(
                                    Icons.Default.Train,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        // App name with gradient-like effect
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.neural),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                            Text(
                                stringResource(R.string.rail),
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                        // Animated tagline
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer { 
                                alpha = expandedProgress
                                scaleY = 0.5f + (0.5f * expandedProgress)
                                translationY = -8f * collapseProgress
                            }
                        ) {
                            Surface(
                                modifier = Modifier.size(6.dp),
                                shape = CircleShape,
                                color = AccentGreen
                            ) {}
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.eco_smart_travel),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Right side - User profile with notification badge style
                // Right side - User profile with notification toggle
                val notificationsEnabled by SettingsRepository.notificationsEnabled.collectAsState()
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification bell - clickable toggle
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { SettingsRepository.toggleNotifications() },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                null,
                                tint = if (notificationsEnabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            // Notification dot - only show when enabled
                            if (notificationsEnabled) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(8.dp)
                                        .background(AccentGreen, CircleShape)
                                )
                            }
                        }
                    }
                    
                    // User avatar with border
                    Box(contentAlignment = Alignment.Center) {
                        // Outer ring
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.3f)
                        ) {}
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onUserClick),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Search bar with GPU-accelerated animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = expandedProgress
                        scaleY = 0.8f + (0.2f * expandedProgress)
                        scaleX = 0.95f + (0.05f * expandedProgress)
                        translationY = -20f * collapseProgress
                    }
                    .height((62 * expandedProgress).dp)
            ) {
                if (expandedProgress > 0.05f) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = LocalAppColors.current.backgroundCard.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = BlueAccent.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Search, null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 15.sp, color = LocalAppColors.current.textPrimary),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text("Search trains, stations...", color = LocalAppColors.current.textMuted, fontSize = 14.sp)
                                    }
                                    inner()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = LocalAppColors.current.textSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// New simplified header without search bar
@Composable
private fun AppHeader(
    onUserClick: () -> Unit,
    isCollapsed: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BluePrimary, BlueSecondary, BluePrimaryDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo and branding
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.2f)) {}
                        Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.15f)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)))) {
                                Icon(Icons.Default.Train, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.neural), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                            Text(stringResource(R.string.rail), color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 21.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.8f)) {}
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.eco_smart_travel), color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                // User profile
                val notificationsEnabled by SettingsRepository.notificationsEnabled.collectAsState()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.size(38.dp).clip(CircleShape).clickable { SettingsRepository.toggleNotifications() },
                        shape = CircleShape, color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff, null, tint = if (notificationsEnabled) Color.White else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            if (notificationsEnabled) Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(8.dp).background(Color.White, CircleShape))
                        }
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) {}
                        Surface(modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onUserClick), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// Search bar as separate section below header
@Composable
private fun SearchBarSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isCollapsed: Boolean,
    appColors: com.neuralrail.neuralrailapp.presentation.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = BlueAccent.copy(alpha = 0.15f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Search, null, tint = BlueAccent, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 15.sp, color = appColors.textPrimary),
                singleLine = true,
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) Text(stringResource(R.string.search_trains_stations), color = appColors.textMuted, fontSize = 14.sp)
                    inner()
                }
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = appColors.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeContentOnly(
    data: EcoCommuteData, 
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // CommunityWatch first, then ImpactBanner
        item(key = "community_watch") { CommunityWatch(onNavigate) }
        item(key = "impact_banner") { ImpactBanner(data) }
        item(key = "quick_services") { QuickServices(onNavigate) }
        item(key = "badges") { Badges(data, onNavigate) }
    }
}

@Composable
private fun HomeContentWithScroll(
    data: EcoCommuteData, 
    onNavigate: (String) -> Unit, 
    appColors: com.neuralrail.neuralrailapp.presentation.theme.AppColors = com.neuralrail.neuralrailapp.presentation.theme.LocalAppColors.current,
    onScrollChange: (Boolean) -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item(key = "community_watch") { CommunityWatch(onNavigate) }
        item(key = "impact_banner") { ImpactBanner(data) }
        item(key = "quick_services") { QuickServices(onNavigate) }
        item(key = "badges") { Badges(data, onNavigate) }
    }
}

@Composable
private fun HomeContent(data: EcoCommuteData, onNavigate: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        item { CommunityWatch(onNavigate) }
        item { ImpactBanner(data) }
        item { QuickServices(onNavigate) }
        item { Badges(data, onNavigate) }
    }
}

@Composable
private fun ImpactBanner(data: EcoCommuteData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(BluePrimaryLight, BluePrimary, BlueSecondary)
                    )
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row with title and streak badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.your_environmental_impact),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("${data.streakDays} ${stringResource(R.string.days)}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Main CO2 stat - more compact
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "${data.totalCO2Saved.toInt()}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(stringResource(R.string.kg_co2), color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.saved_this_month), color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    }
                }
                
                Spacer(Modifier.height(10.dp))
                
                // Stats row at bottom - more compact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStatItem(Icons.Default.Train, "${data.totalTrips}", stringResource(R.string.trips))
                    MiniStatItem(Icons.Default.Park, "${(data.totalCO2Saved / 21).toInt()}", stringResource(R.string.trees))
                    MiniStatItem(Icons.Default.EmojiEvents, "${data.badges.count { it.isUnlocked }}", stringResource(R.string.badges))
                }
            }
        }
    }
}

@Composable
private fun MiniStatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@Composable
private fun QuickServices(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.quick_services),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LocalAppColors.current.textPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LocalAppColors.current.backgroundCard,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ServiceItem(Icons.Default.ConfirmationNumber, stringResource(R.string.book_ticket), BlueAccent) { onNavigate("planner") }
                ServiceItem(Icons.Default.Bolt, stringResource(R.string.energy), AccentCyan) { onNavigate("green_rail") }
                ServiceItem(Icons.Default.Eco, stringResource(R.string.support), AccentGreen) { onNavigate("carbon_offset") }
                ServiceItem(Icons.Default.School, stringResource(R.string.learn), AccentYellow) { onNavigate("education_hub") }
            }
        }
    }
}

@Composable
private fun ServiceItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = LocalAppColors.current.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun WeeklyProgress(data: EcoCommuteData, onNavigate: (String) -> Unit) {
    val progress = (data.weeklyProgress / data.weeklyGoal).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Weekly Goal",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LocalAppColors.current.textPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigate("weekly_goal") },
            shape = RoundedCornerShape(16.dp),
            color = LocalAppColors.current.backgroundCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${data.weeklyProgress.toInt()} / ${data.weeklyGoal.toInt()} kg",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LocalAppColors.current.textPrimary
                        )
                        Text(
                            "CO₂ saved this week",
                            fontSize = 12.sp,
                            color = LocalAppColors.current.textSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "$percentage%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = LocalAppColors.current.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = AccentGreen,
                    trackColor = AccentGreen.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (percentage >= 100) "🎉 Goal achieved! Amazing work!" 
                        else "Keep up the great progress!",
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                    Text(
                        "View details",
                        fontSize = 12.sp,
                        color = BlueAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityWatch(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            stringResource(R.string.community_impact),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LocalAppColors.current.textPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("community_watch") },
            shape = RoundedCornerShape(16.dp),
            color = LocalAppColors.current.backgroundCard,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = AccentOrange.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReportProblem, null, tint = AccentOrange, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.report_problem),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.community_watch_desc),
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentOrange
                ) {
                    Text(
                        stringResource(R.string.report_issue),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun Badges(data: EcoCommuteData, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.achievements),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = LocalAppColors.current.textPrimary
            )
            Text(
                stringResource(R.string.read_more),
                color = BlueAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onNavigate("achievements") }
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(data.badges) { badge -> BadgeCard(badge) }
        }
    }
}

@Composable
private fun BadgeCard(badge: com.neuralrail.neuralrailapp.data.models.Badge) {
    val badgeColor = when (badge.iconType) {
        com.neuralrail.neuralrailapp.data.models.BadgeType.BRONZE -> BronzeBadge
        com.neuralrail.neuralrailapp.data.models.BadgeType.SILVER -> SilverBadge
        com.neuralrail.neuralrailapp.data.models.BadgeType.GOLD -> GoldBadge
        com.neuralrail.neuralrailapp.data.models.BadgeType.PLATINUM -> PlatinumBadge
        com.neuralrail.neuralrailapp.data.models.BadgeType.SPECIAL -> AccentOrange
    }
    
    val appColors = LocalAppColors.current
    
    Surface(
        modifier = Modifier.size(width = 100.dp, height = 120.dp),
        shape = RoundedCornerShape(14.dp),
        color = appColors.backgroundCard,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy icon with proper coloring
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (badge.isUnlocked) badgeColor.copy(alpha = 0.25f) else appColors.divider.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        null,
                        tint = if (badge.isUnlocked) badgeColor else appColors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // Badge name - properly centered and sized
            Text(
                badge.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (badge.isUnlocked) appColors.textPrimary else appColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
            // Progress bar for locked badges
            if (!badge.isUnlocked) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AccentOrange,
                    trackColor = appColors.divider
                )
            }
        }
    }
}

@Composable
private fun RecentTrips(data: EcoCommuteData) {
    var expandedTripId by remember { mutableStateOf<String?>(null) }
    var showBookingMessage by remember { mutableStateOf(false) }
    var bookedTripRoute by remember { mutableStateOf("") }
    
    Box {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Trips",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LocalAppColors.current.textPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BlueAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        "${data.recentTrips.size} trips",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = BlueAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.recentTrips.take(3).forEach { trip -> 
                    TripCard(
                        trip = trip,
                        isExpanded = expandedTripId == trip.id,
                        onToggle = { expandedTripId = if (expandedTripId == trip.id) null else trip.id },
                        onRepeatTrip = {
                            bookedTripRoute = "${trip.from} → ${trip.to}"
                            showBookingMessage = true
                            expandedTripId = null
                        }
                    )
                }
            }
        }
        
        // Booking confirmation message
        androidx.compose.animation.AnimatedVisibility(
            visible = showBookingMessage,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AccentGreen,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Trip Booked! 🎉",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            bookedTripRoute,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Auto-dismiss after 3 seconds
            LaunchedEffect(showBookingMessage) {
                if (showBookingMessage) {
                    kotlinx.coroutines.delay(3000)
                    showBookingMessage = false
                }
            }
        }
    }
}

@Composable
private fun TripCard(
    trip: com.neuralrail.neuralrailapp.data.models.EcoTrip,
    isExpanded: Boolean = false,
    onToggle: () -> Unit = {},
    onRepeatTrip: () -> Unit = {}
) {
    val modeIcon = when (trip.mode) {
        com.neuralrail.neuralrailapp.data.models.TransportMode.RAIL -> Icons.Default.Train
        com.neuralrail.neuralrailapp.data.models.TransportMode.METRO -> Icons.Default.Subway
        com.neuralrail.neuralrailapp.data.models.TransportMode.BUS -> Icons.Default.DirectionsBus
        else -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
    
    val modeName = when (trip.mode) {
        com.neuralrail.neuralrailapp.data.models.TransportMode.RAIL -> "Rail"
        com.neuralrail.neuralrailapp.data.models.TransportMode.METRO -> "Metro"
        com.neuralrail.neuralrailapp.data.models.TransportMode.BUS -> "Bus"
        com.neuralrail.neuralrailapp.data.models.TransportMode.WALK -> "Walk"
        com.neuralrail.neuralrailapp.data.models.TransportMode.CYCLE -> "Cycle"
    }
    
    val modeColor = when (trip.mode) {
        com.neuralrail.neuralrailapp.data.models.TransportMode.RAIL -> BlueAccent
        com.neuralrail.neuralrailapp.data.models.TransportMode.METRO -> AccentCyan
        com.neuralrail.neuralrailapp.data.models.TransportMode.BUS -> AccentGreen
        else -> TextSecondary
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = if (isExpanded) modeColor.copy(alpha = 0.08f) else BackgroundCard
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = modeColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(modeIcon, null, tint = modeColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Route - now with proper wrapping
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            trip.from,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = LocalAppColors.current.textPrimary
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            null,
                            tint = modeColor,
                            modifier = Modifier.padding(horizontal = 6.dp).size(16.dp)
                        )
                        Text(
                            trip.to,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = LocalAppColors.current.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(trip.date, fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
                        Text(" • ", fontSize = 12.sp, color = LocalAppColors.current.textMuted)
                        Text("${trip.distance} km", fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
                        Text(" • ", fontSize = 12.sp, color = LocalAppColors.current.textMuted)
                        Text(modeName, fontSize = 12.sp, color = modeColor, fontWeight = FontWeight.Medium)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "-${trip.co2Saved} kg",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = LocalAppColors.current.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Expanded details
            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                val localAppColors = LocalAppColors.current
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = localAppColors.backgroundCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TripDetailItem(Icons.Default.Route, "Distance", "${trip.distance} km", modeColor)
                            TripDetailItem(Icons.Default.Eco, "CO₂ Saved", "${trip.co2Saved} kg", AccentGreen)
                            TripDetailItem(Icons.Default.CalendarToday, "Date", trip.date, BlueAccent)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onRepeatTrip),
                                shape = RoundedCornerShape(8.dp),
                                color = modeColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Repeat Trip",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
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
private fun TripDetailItem(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LocalAppColors.current.textPrimary)
        Text(label, fontSize = 10.sp, color = LocalAppColors.current.textSecondary)
    }
}
