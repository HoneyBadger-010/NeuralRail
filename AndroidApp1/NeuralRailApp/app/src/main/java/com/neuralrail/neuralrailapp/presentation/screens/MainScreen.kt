package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.models.EcoCommuteData
import com.neuralrail.neuralrailapp.data.models.EducationContent
import com.neuralrail.neuralrailapp.data.remote.UserDto
import com.neuralrail.neuralrailapp.presentation.viewmodels.*

sealed class BottomNavItem(val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val labelResId: Int) {
    data object Home : BottomNavItem("home", Icons.Filled.Home, Icons.Outlined.Home, R.string.nav_home)
    data object TrainStatus : BottomNavItem("train_status", Icons.Filled.Train, Icons.Outlined.Train, R.string.nav_trains)
    data object QRScanner : BottomNavItem("qr_scanner", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner, R.string.nav_scan)
    data object Challenges : BottomNavItem("challenges", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, R.string.nav_challenges)
    data object More : BottomNavItem("more", Icons.Filled.Menu, Icons.Outlined.Menu, R.string.nav_more)
}

private val moreSubScreens = setOf("carbon_offset", "community_watch", "education_hub", "green_rail", "planner", "profile", "settings", "about", "achievements", "article_detail", "report_wastage")

// Get tab index for animation direction
private fun getTabIndex(route: String?): Int = when (route) {
    "home" -> 0
    "train_status" -> 1
    "qr_scanner" -> 2
    "challenges" -> 3
    "more" -> 4
    in moreSubScreens -> 5 // Sub-screens are "after" more
    else -> -1
}

@Composable
fun MainScreen(factory: ViewModelFactory, currentUser: UserDto? = null, onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.TrainStatus, BottomNavItem.QRScanner, BottomNavItem.Challenges, BottomNavItem.More)
    val selectedRoute = when { currentRoute in moreSubScreens -> BottomNavItem.More.route; else -> currentRoute }
    
    // Track navigation direction
    var navigationDirection by remember { mutableIntStateOf(1) } // 1 = forward (right), -1 = backward (left)
    
    // Store selected article for detail view
    var selectedArticle by remember { mutableStateOf<EducationContent?>(null) }

    Scaffold(
        bottomBar = {
            CustomBottomNavBar(items = bottomNavItems, currentRoute = selectedRoute, onItemClick = { item ->
                // Determine animation direction based on tab positions
                val currentIndex = getTabIndex(currentRoute)
                val targetIndex = getTabIndex(item.route)
                navigationDirection = if (targetIndex < currentIndex) -1 else 1
                
                // Pop back stack if on a sub-screen
                if (currentRoute in moreSubScreens) {
                    navController.popBackStack()
                }
                // Navigate to the target
                navController.navigate(item.route) {
                    popUpTo(BottomNavItem.Home.route) {
                        inclusive = (item.route == BottomNavItem.Home.route)
                    }
                    launchSingleTop = true
                }
            })
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues),
            // Dynamic enter transition based on direction
            enterTransition = {
                val fromIndex = getTabIndex(initialState.destination.route)
                val toIndex = getTabIndex(targetState.destination.route)
                val direction = if (toIndex > fromIndex) 1 else -1
                
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> direction * fullWidth }
                ) + fadeIn(tween(250))
            },
            exitTransition = {
                val fromIndex = getTabIndex(initialState.destination.route)
                val toIndex = getTabIndex(targetState.destination.route)
                val direction = if (toIndex > fromIndex) -1 else 1
                
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> direction * fullWidth / 3 }
                ) + fadeOut(tween(200))
            },
            // Pop transitions (going back)
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> -fullWidth }
                ) + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> fullWidth }
                ) + fadeOut(tween(200))
            }
        ) {
            composable(BottomNavItem.Home.route) {
                val vm: EcoCommuteViewModel = viewModel(factory = factory)
                HomeScreen(viewModel = vm, currentUser = currentUser, onNavigate = { route -> navController.navigate(route) }, onLogout = onLogout)
            }
            composable(BottomNavItem.TrainStatus.route) { val vm: TrainStatusViewModel = viewModel(factory = factory); TrainStatusScreen(viewModel = vm) }
            composable(BottomNavItem.QRScanner.route) { val vm: QRScannerViewModel = viewModel(factory = factory); QRScannerScreen(viewModel = vm) }
            composable(BottomNavItem.Challenges.route) { val vm: GreenChallengeViewModel = viewModel(factory = factory); GreenChallengeScreen(viewModel = vm) }
            composable(BottomNavItem.More.route) { MoreScreen(factory = factory, onNavigate = { route -> navController.navigate(route) }) }
            // Sub-screens
            composable("report_wastage") {
                 val vm: ReportWastageViewModel = viewModel(factory = factory)
                 ReportWastageScreen(
                     viewModel = vm,
                     onBack = { navController.popBackStack() },
                     userId = currentUser?.user_id?.toString() ?: "anonymous"
                 )
            }
            composable("carbon_offset") { val vm: CarbonOffsetViewModel = viewModel(factory = factory); CarbonOffsetScreen(viewModel = vm, onBack = { navController.popBackStack() }) }
            composable("community_watch") { val vm: CommunityWatchViewModel = viewModel(factory = factory); CommunityWatchScreen(viewModel = vm, onBack = { navController.popBackStack() }) }
            composable("education_hub") { 
                val vm: EducationHubViewModel = viewModel(factory = factory)
                EducationHubScreen(
                    viewModel = vm, 
                    onBack = { navController.popBackStack() },
                    onArticleClick = { article ->
                        selectedArticle = article
                        navController.navigate("article_detail")
                    }
                )
            }
            composable("green_rail") { val vm: GreenRailViewModel = viewModel(factory = factory); GreenRailScreen(viewModel = vm, onBack = { navController.popBackStack() }) }
            composable("planner") { val vm: SmartPlannerViewModel = viewModel(factory = factory); SmartPlannerScreen(viewModel = vm, onBack = { navController.popBackStack() }) }
            // User drawer screens
            composable("profile") { UserProfileScreen(user = currentUser, onBack = { navController.popBackStack() }) }
            composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { navController.popBackStack() }) }
            // Achievements screen
            composable("achievements") {
                val vm: EcoCommuteViewModel = viewModel(factory = factory)
                val state by vm.ecoCommuteState.collectAsState()
                val badges = when (val s = state) {
                    is com.neuralrail.neuralrailapp.data.UiState.Success -> s.data.badges
                    else -> emptyList()
                }
                AchievementsScreen(badges = badges, onBack = { navController.popBackStack() })
            }
            // Article detail screen with custom transitions (slide from right)
            composable(
                "article_detail",
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) + fadeIn(tween(250))
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> fullWidth }
                    ) + fadeOut(tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                    ) + fadeIn(tween(250))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> fullWidth }
                    ) + fadeOut(tween(200))
                }
            ) {
                selectedArticle?.let { article ->
                    ArticleDetailScreen(
                        content = article,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}


@Composable
private fun CustomBottomNavBar(items: List<BottomNavItem>, currentRoute: String?, onItemClick: (BottomNavItem) -> Unit) {
    val appColors = com.neuralrail.neuralrailapp.presentation.theme.LocalAppColors.current
    
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().height(65.dp).align(Alignment.BottomCenter), 
            shape = RoundedCornerShape(20.dp), 
            colors = CardDefaults.cardColors(containerColor = appColors.backgroundCard), 
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                items.forEachIndexed { index, item ->
                    if (index == 2) Spacer(modifier = Modifier.width(56.dp))
                    else NavBarItem(item = item, isSelected = currentRoute == item.route, onClick = { onItemClick(item) })
                }
            }
        }
        
        val qrItem = items[2]
        val isQRSelected = currentRoute == qrItem.route
        val fabInteraction = remember { MutableInteractionSource() }
        val isFabPressed by fabInteraction.collectIsPressedAsState()
        
        val fabScale by animateFloatAsState(
            targetValue = when { isFabPressed -> 0.85f; isQRSelected -> 1.12f; else -> 1f },
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
            label = "fab_scale"
        )
        
        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-16).dp)) {
            FloatingActionButton(
                onClick = { onItemClick(qrItem) },
                modifier = Modifier.size(56.dp).scale(fabScale).shadow(12.dp, CircleShape),
                shape = CircleShape,
                containerColor = com.neuralrail.neuralrailapp.presentation.theme.BluePrimary,
                contentColor = Color.White,
                interactionSource = fabInteraction
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan QR", modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun NavBarItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val appColors = com.neuralrail.neuralrailapp.presentation.theme.LocalAppColors.current
    
    val selectedColor = com.neuralrail.neuralrailapp.presentation.theme.BluePrimary
    val unselectedColor = appColors.textSecondary
    
    val scale by animateFloatAsState(
        targetValue = when { isPressed -> 0.9f; isSelected -> 1.05f; else -> 1f },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "item_scale"
    )
    
    val label = stringResource(item.labelResId)
    
    // Use IconButton for better touch target
    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale).size(56.dp),
        interactionSource = interactionSource
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = label,
                tint = if (isSelected) selectedColor else unselectedColor,
                modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) selectedColor else unselectedColor,
                maxLines = 1
            )
        }
    }
}
