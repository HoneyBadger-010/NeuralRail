package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.models.Badge
import com.neuralrail.neuralrailapp.data.models.BadgeType
import com.neuralrail.neuralrailapp.presentation.theme.*

@Composable
fun AchievementsScreen(
    badges: List<Badge>,
    onBack: () -> Unit = {}
) {
    val unlockedBadges = badges.filter { it.isUnlocked }
    val lockedBadges = badges.filter { !it.isUnlocked }
    val appColors = LocalAppColors.current
    
    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header
        AchievementsHeader(
            unlockedCount = unlockedBadges.size,
            totalCount = badges.size,
            onBack = onBack
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Summary
            item { StatsSummaryCard(unlockedBadges.size, badges.size) }
            
            // Unlocked Badges Section
            if (unlockedBadges.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.unlocked),
                        subtitle = stringResource(R.string.achievements_earned, unlockedBadges.size),
                        icon = Icons.Default.EmojiEvents,
                        color = AccentGreen
                    )
                }
                items(unlockedBadges) { badge ->
                    AchievementCard(badge = badge, isUnlocked = true)
                }
            }
            
            // Locked Badges Section
            if (lockedBadges.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(
                        title = stringResource(R.string.in_progress_title),
                        subtitle = stringResource(R.string.achievements_remaining, lockedBadges.size),
                        icon = Icons.Default.Lock,
                        color = LocalAppColors.current.textSecondary
                    )
                }
                items(lockedBadges) { badge ->
                    AchievementCard(badge = badge, isUnlocked = false)
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AchievementsHeader(
    unlockedCount: Int,
    totalCount: Int,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BluePrimary,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.achievements),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.unlocked_of_total, unlockedCount, totalCount),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatsSummaryCard(unlockedCount: Int, totalCount: Int) {
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
    val percentage = (progress * 100).toInt()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(BluePrimaryDark, BluePrimary, BlueSecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.your_progress),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "$percentage",
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 48.sp
                            )
                            Text(
                                "%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.completion_rate),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    
                    // Circular progress indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(80.dp),
                            color = AccentGreen,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$unlockedCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                stringResource(R.string.of_total, totalCount),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AccentGreen,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = LocalAppColors.current.textPrimary
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = LocalAppColors.current.textSecondary
            )
        }
    }
}

@Composable
private fun AchievementCard(badge: Badge, isUnlocked: Boolean) {
    val appColors = LocalAppColors.current
    
    // Vibrant colors for unlocked badges
    val badgeColor = when (badge.iconType) {
        BadgeType.BRONZE -> Color(0xFFCD7F32) // Bronze
        BadgeType.SILVER -> Color(0xFFC0C0C0) // Silver
        BadgeType.GOLD -> Color(0xFFFFD700) // Gold
        BadgeType.PLATINUM -> Color(0xFFE5E4E2) // Platinum
        BadgeType.SPECIAL -> AccentOrange
    }
    
    // Muted colors for in-progress badges
    val inProgressColor = AccentOrange
    
    val badgeIcon = when (badge.iconType) {
        BadgeType.BRONZE -> Icons.Default.Star
        BadgeType.SILVER -> Icons.Default.StarHalf
        BadgeType.GOLD -> Icons.Default.EmojiEvents
        BadgeType.PLATINUM -> Icons.Default.Diamond
        BadgeType.SPECIAL -> Icons.Default.AutoAwesome
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = appColors.backgroundCard,
        shadowElevation = if (isUnlocked) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Icon - vibrant for unlocked, muted for in-progress
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (isUnlocked) badgeColor.copy(alpha = 0.25f) else inProgressColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        badgeIcon,
                        null,
                        tint = if (isUnlocked) badgeColor else inProgressColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            // Badge Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        badge.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isUnlocked) appColors.textPrimary else appColors.textSecondary
                    )
                    if (isUnlocked) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    stringResource(R.string.unlocked),
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    badge.description,
                    fontSize = 12.sp,
                    color = appColors.textSecondary,
                    lineHeight = 16.sp
                )
                
                // Progress bar for in-progress badges
                if (!isUnlocked) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { badge.progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = inProgressColor,
                            trackColor = appColors.divider
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${(badge.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = inProgressColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Badge Type indicator
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isUnlocked) badgeColor.copy(alpha = 0.2f) else appColors.divider.copy(alpha = 0.5f)
            ) {
                Text(
                    badge.iconType.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) badgeColor else appColors.textMuted
                )
            }
        }
    }
}
