package com.neuralrail.neuralrailapp.presentation.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.GreenChallengeViewModel

@Composable
fun GreenChallengeScreen(viewModel: GreenChallengeViewModel) {
    val challengesState by viewModel.challengesState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val acceptedChallenges by viewModel.acceptedChallenges.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val countdowns by viewModel.countdowns.collectAsState()
    val appColors = LocalAppColors.current
    
    var selectedChallenge by remember { mutableStateOf<DailyChallenge?>(null) }
    var showAcceptedDialog by remember { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf<String?>(null) }
    var showShareDialog by remember { mutableStateOf<DailyChallenge?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        ChallengeHeader()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Card
            item {
                when (val stats = statsState) {
                    is UiState.Success -> StatsCard(stats.data)
                    is UiState.Loading -> LoadingCard(appColors)
                    is UiState.Error -> ErrorCard(stats.message)
                }
            }
            
            // Streak Visualization
            item {
                when (val stats = statsState) {
                    is UiState.Success -> StreakVisualization(stats.data, appColors)
                    else -> {}
                }
            }

            
            // Active Challenges Section
            if (acceptedChallenges.values.any { it.isActive }) {
                item {
                    Text(
                        "🔥 Active Challenges",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = appColors.textPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                items(acceptedChallenges.values.filter { it.isActive }.toList()) { accepted ->
                    ActiveChallengeCard(
                        accepted = accepted,
                        appColors = appColors,
                        timeRemaining = viewModel.formatTimeRemaining(countdowns[accepted.challenge.id] ?: 0),
                        progress = viewModel.getProgressPercent(accepted.challenge.id),
                        onAbandon = { showAbandonDialog = accepted.challenge.id },
                        onShare = { showShareDialog = accepted.challenge },
                        onUpdateProgress = { viewModel.updateProgress(accepted.challenge.id, accepted.progress + 1) }
                    )
                }
            }
            
            // Category Filter
            item {
                CategoryFilter(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setFilter(it) },
                    appColors = appColors
                )
            }
            
            // Type Filter
            item {
                TypeFilter(
                    selectedType = selectedType,
                    onTypeSelected = { viewModel.setTypeFilter(it) },
                    appColors = appColors
                )
            }

            item {
                Text(
                    "Available Missions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = appColors.textPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text("Complete challenges to earn points", fontSize = 13.sp, color = appColors.textSecondary)
            }
            
            when (val challenges = challengesState) {
                is UiState.Success -> {
                    val filtered = challenges.data.filter { challenge ->
                        val categoryMatch = selectedCategory == null || challenge.category == selectedCategory
                        val typeMatch = selectedType == null || challenge.type == selectedType
                        categoryMatch && typeMatch
                    }
                    items(filtered) { challenge ->
                        val isAccepted = acceptedChallenges.containsKey(challenge.id)
                        ChallengeCard(
                            challenge = challenge, 
                            appColors = appColors,
                            isAccepted = isAccepted,
                            timeRemaining = viewModel.formatTimeRemaining(countdowns[challenge.id] ?: 0),
                            onViewDetails = { selectedChallenge = challenge }
                        )
                    }
                }
                is UiState.Loading -> item { LoadingCard(appColors) }
                is UiState.Error -> item { ErrorCard(challenges.message) }
            }
            
            // Leaderboard
            item {
                when (val stats = statsState) {
                    is UiState.Success -> LeaderboardCard(stats.data, appColors)
                    else -> {}
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    
    // Challenge Detail Dialog
    selectedChallenge?.let { challenge ->
        ChallengeDetailDialog(
            challenge = challenge,
            appColors = appColors,
            isAccepted = acceptedChallenges.containsKey(challenge.id),
            timeRemaining = viewModel.formatTimeRemaining(countdowns[challenge.id] ?: 0),
            onDismiss = { selectedChallenge = null },
            onAccept = {
                viewModel.acceptChallenge(challenge)
                selectedChallenge = null
                showAcceptedDialog = true
            }
        )
    }
    
    // Challenge Accepted Popup
    if (showAcceptedDialog) {
        ChallengeAcceptedDialog(onDismiss = { showAcceptedDialog = false })
    }
    
    // Abandon Challenge Dialog
    showAbandonDialog?.let { challengeId ->
        AbandonChallengeDialog(
            onConfirm = {
                viewModel.abandonChallenge(challengeId)
                showAbandonDialog = null
            },
            onDismiss = { showAbandonDialog = null }
        )
    }
    
    // Share Dialog
    showShareDialog?.let { challenge ->
        ShareChallengeDialog(
            challenge = challenge,
            shareText = viewModel.shareProgress(challenge),
            onDismiss = { showShareDialog = null }
        )
    }
}

@Composable
private fun ChallengeHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BluePrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = AccentYellow, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.green_challenges), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(stringResource(R.string.earn_points_leaderboard), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}


@Composable
private fun StatsCard(stats: UserChallengeStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BluePrimaryDark, BluePrimary, BlueSecondary)))
                .padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(Icons.Default.Star, "${stats.totalPoints}", stringResource(R.string.points), AccentYellow)
                StatItem(Icons.Default.LocalFireDepartment, "${stats.currentStreak}", stringResource(R.string.streak), AccentOrange)
                StatItem(Icons.Default.Leaderboard, "#${stats.rank}", stringResource(R.string.rank), AccentCyan)
            }
        }
    }
}

@Composable
private fun StreakVisualization(stats: UserChallengeStats, appColors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = AccentOrange, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.streak_progress), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appColors.textPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentOrange.copy(alpha = 0.2f)
                ) {
                    Text(
                        stringResource(R.string.best_days, stats.longestStreak),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = AccentOrange,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Weekly streak visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.streakHistory.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.completed) AccentGreen else appColors.background
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.completed) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    "${day.challengesCompleted}",
                                    color = appColors.textMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(day.date, fontSize = 11.sp, color = appColors.textSecondary)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Streak stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakStatItem(stringResource(R.string.daily), stats.dailyStreak, AccentGreen)
                StreakStatItem(stringResource(R.string.weekly), stats.weeklyStreak, AccentCyan)
                StreakStatItem(stringResource(R.string.longest), stats.longestStreak, AccentYellow)
            }
        }
    }
}

@Composable
private fun StreakStatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String, iconColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}


@Composable
private fun CategoryFilter(
    selectedCategory: ChallengeCategory?,
    onCategorySelected: (ChallengeCategory?) -> Unit,
    appColors: AppColors
) {
    val dailyText = stringResource(R.string.daily)
    val weeklyText = stringResource(R.string.weekly)
    val specialText = stringResource(R.string.special)
    val communityText = stringResource(R.string.community)
    
    Column {
        Text(stringResource(R.string.category), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textSecondary)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text(stringResource(R.string.all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(ChallengeCategory.values().toList()) { category ->
                val categoryLabel = when (category) {
                    ChallengeCategory.DAILY -> dailyText
                    ChallengeCategory.WEEKLY -> weeklyText
                    ChallengeCategory.SPECIAL -> specialText
                    ChallengeCategory.COMMUNITY -> communityText
                }
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(categoryLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun TypeFilter(
    selectedType: ChallengeType?,
    onTypeSelected: (ChallengeType?) -> Unit,
    appColors: AppColors
) {
    val travelText = stringResource(R.string.travel)
    val walkText = stringResource(R.string.walk)
    val offPeakText = stringResource(R.string.off_peak)
    val shareText = stringResource(R.string.share)
    val reportText = stringResource(R.string.report_problem)
    
    Column {
        Text(stringResource(R.string.type), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textSecondary)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text(stringResource(R.string.all)) },
                    leadingIcon = { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(ChallengeType.values().toList()) { type ->
                val icon = when (type) {
                    ChallengeType.TRAVEL -> Icons.Default.Train
                    ChallengeType.WALK -> Icons.Default.DirectionsWalk
                    ChallengeType.OFF_PEAK -> Icons.Default.Schedule
                    ChallengeType.SHARE -> Icons.Default.Share
                    ChallengeType.REPORT -> Icons.Default.Report
                }
                val typeLabel = when (type) {
                    ChallengeType.TRAVEL -> travelText
                    ChallengeType.WALK -> walkText
                    ChallengeType.OFF_PEAK -> offPeakText
                    ChallengeType.SHARE -> shareText
                    ChallengeType.REPORT -> reportText
                }
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(typeLabel) },
                    leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun ActiveChallengeCard(
    accepted: AcceptedChallenge,
    appColors: AppColors,
    timeRemaining: String,
    progress: Float,
    onAbandon: () -> Unit,
    onShare: () -> Unit,
    onUpdateProgress: () -> Unit
) {
    val challenge = accepted.challenge
    val isExpiringSoon = timeRemaining.contains("m") && !timeRemaining.contains("h")
    
    // Pulsing animation for expiring challenges
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = appColors.backgroundCard,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentOrange.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, null, tint = AccentOrange, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appColors.textPrimary)
                        Text("+${challenge.points} pts", fontSize = 12.sp, color = AccentYellow, fontWeight = FontWeight.Medium)
                    }
                }
                
                // Timer with warning color if expiring soon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpiringSoon) AccentRed.copy(alpha = pulseAlpha) else AccentOrange.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isExpiringSoon) Color.White else AccentOrange
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            timeRemaining,
                            color = if (isExpiringSoon) Color.White else AccentOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress", fontSize = 12.sp, color = appColors.textSecondary)
                    Text("${accepted.progress}/${challenge.targetProgress}", fontSize = 12.sp, color = appColors.textPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AccentGreen,
                    trackColor = appColors.background
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Action buttons - two rows layout
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // +1 Progress Button - Full width on top
                Button(
                    onClick = onUpdateProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Progress (+1)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                
                // Abandon and Share buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Abandon Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onAbandon),
                        shape = RoundedCornerShape(10.dp),
                        color = AccentRed.copy(alpha = 0.12f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Abandon", color = AccentRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Share Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onShare),
                        shape = RoundedCornerShape(10.dp),
                        color = BluePrimary.copy(alpha = 0.12f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share", color = BluePrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ChallengeCard(
    challenge: DailyChallenge, 
    appColors: AppColors, 
    isAccepted: Boolean,
    timeRemaining: String,
    onViewDetails: () -> Unit
) {
    val typeIcon = when (challenge.type) {
        ChallengeType.TRAVEL -> Icons.Default.Train
        ChallengeType.WALK -> Icons.Default.DirectionsWalk
        ChallengeType.OFF_PEAK -> Icons.Default.Schedule
        ChallengeType.SHARE -> Icons.Default.Share
        ChallengeType.REPORT -> Icons.Default.Report
    }
    
    val categoryColor = when (challenge.category) {
        ChallengeCategory.DAILY -> AccentGreen
        ChallengeCategory.WEEKLY -> BluePrimary
        ChallengeCategory.SPECIAL -> AccentYellow
        ChallengeCategory.COMMUNITY -> AccentCyan
    }
    
    val difficultyColor = when (challenge.difficulty) {
        ChallengeDifficulty.EASY -> AccentGreen
        ChallengeDifficulty.MEDIUM -> AccentOrange
        ChallengeDifficulty.HARD -> AccentRed
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(typeIcon, null, tint = categoryColor, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appColors.textPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = categoryColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    challenge.category.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = categoryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = difficultyColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    challenge.difficulty.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = difficultyColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Surface(shape = RoundedCornerShape(8.dp), color = AccentYellow.copy(alpha = 0.2f)) {
                    Text(
                        "+${challenge.points} pts",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text(challenge.description, fontSize = 13.sp, color = appColors.textSecondary, lineHeight = 18.sp)
            
            Spacer(Modifier.height(8.dp))
            
            // Timer and progress info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = appColors.textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(timeRemaining, fontSize = 12.sp, color = appColors.textSecondary)
                }
                
                if (challenge.targetProgress > 1) {
                    Text(
                        "Target: ${challenge.targetProgress}",
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            when {
                challenge.isCompleted -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = AccentGreen.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Completed!", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                isAccepted -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = AccentOrange.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Pending, null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("In Progress", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Accept Challenge", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
private fun ChallengeDetailDialog(
    challenge: DailyChallenge,
    appColors: AppColors,
    isAccepted: Boolean,
    timeRemaining: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val typeIcon = when (challenge.type) {
        ChallengeType.TRAVEL -> Icons.Default.Train
        ChallengeType.WALK -> Icons.Default.DirectionsWalk
        ChallengeType.OFF_PEAK -> Icons.Default.Schedule
        ChallengeType.SHARE -> Icons.Default.Share
        ChallengeType.REPORT -> Icons.Default.Report
    }
    
    val categoryColor = when (challenge.category) {
        ChallengeCategory.DAILY -> AccentGreen
        ChallengeCategory.WEEKLY -> BluePrimary
        ChallengeCategory.SPECIAL -> AccentYellow
        ChallengeCategory.COMMUNITY -> AccentCyan
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.backgroundCard,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(typeIcon, null, tint = categoryColor, modifier = Modifier.size(40.dp))
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    challenge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AccentYellow.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "+${challenge.points} Points",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = categoryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            challenge.category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Challenge Details",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appColors.textPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    challenge.description,
                    fontSize = 14.sp,
                    color = appColors.textSecondary,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = appColors.background
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailRow("Type", challenge.type.name.replace("_", " "), appColors)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Category", challenge.category.name, appColors)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Time Left", timeRemaining, appColors)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Target", "${challenge.targetProgress}", appColors)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Difficulty", color = appColors.textSecondary, fontSize = 13.sp)
                            Text(
                                challenge.difficulty.name,
                                color = when (challenge.difficulty) {
                                    ChallengeDifficulty.EASY -> AccentGreen
                                    ChallengeDifficulty.MEDIUM -> AccentOrange
                                    ChallengeDifficulty.HARD -> AccentRed
                                },
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isAccepted && !challenge.isCompleted) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Accept Challenge", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = appColors.textSecondary)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, appColors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = appColors.textSecondary, fontSize = 13.sp)
        Text(value, color = appColors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun ChallengeAcceptedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalAppColors.current.backgroundCard,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = AccentGreen.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(48.dp))
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Challenge Accepted!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = LocalAppColors.current.textPrimary
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Good luck! Complete the challenge to earn your points.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Let's Go!", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun AbandonChallengeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalAppColors.current.backgroundCard,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = AccentRed.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = AccentRed, modifier = Modifier.size(48.dp))
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Abandon Challenge?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LocalAppColors.current.textPrimary
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Your progress will be lost and you won't earn points for this challenge.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Abandon", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Going", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}


@Composable
private fun ShareChallengeDialog(
    challenge: DailyChallenge,
    shareText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalAppColors.current.backgroundCard,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = BluePrimary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Share, null, tint = BluePrimary, modifier = Modifier.size(48.dp))
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Share Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LocalAppColors.current.textPrimary
                )
                
                Spacer(Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LocalAppColors.current.background
                ) {
                    Text(
                        shareText,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LocalAppColors.current.textSecondary)
            }
        }
    )
}

@Composable
private fun LeaderboardCard(stats: UserChallengeStats, appColors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Leaderboard, null, tint = AccentYellow, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Weekly Leaderboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appColors.textPrimary)
            }
            Spacer(Modifier.height(16.dp))
            
            stats.weeklyLeaderboard.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rankColor = when (entry.rank) {
                        1 -> AccentYellow
                        2 -> SilverBadge
                        3 -> BronzeBadge
                        else -> appColors.textMuted
                    }
                    
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(rankColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${entry.rank}", color = if (entry.rank <= 3) BluePrimaryDark else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.userName,
                            fontWeight = if (entry.userName == "You") FontWeight.Bold else FontWeight.Normal,
                            color = if (entry.userName == "You") BluePrimary else appColors.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.size(12.dp), tint = AccentOrange)
                            Spacer(Modifier.width(2.dp))
                            Text("${entry.streak} day streak", fontSize = 11.sp, color = appColors.textSecondary)
                        }
                    }
                    
                    Text(
                        "${entry.points} pts",
                        color = if (entry.userName == "You") BluePrimary else appColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(appColors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(12.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BluePrimary)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AccentRed.copy(alpha = 0.2f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, color = AccentRed)
        }
    }
}
