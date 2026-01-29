package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.remote.UserDto
import com.neuralrail.neuralrailapp.presentation.theme.*

@Composable
fun UserProfileScreen(user: UserDto?, onBack: () -> Unit = {}) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Icon(Icons.Default.Person, null, tint = AccentOrange, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.my_profile), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.manage_account), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ProfileHeader(user) }
            item { StatsCard(user, appColors) }
            item { Text(stringResource(R.string.account_settings), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { ProfileMenuItem(Icons.Default.Edit, stringResource(R.string.edit_profile), stringResource(R.string.update_personal_info), AccentOrange, appColors) }
            item { ProfileMenuItem(Icons.Default.Notifications, stringResource(R.string.notifications), stringResource(R.string.manage_notifications), AccentYellow, appColors) }
            item { ProfileMenuItem(Icons.Default.Security, stringResource(R.string.privacy_security), stringResource(R.string.password_security), AccentGreen, appColors) }
            item { ProfileMenuItem(Icons.Default.Language, stringResource(R.string.language), stringResource(R.string.english), BlueAccent, appColors) }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserDto?) {
    val userText = stringResource(R.string.user)
    val ecoWarriorText = stringResource(R.string.eco_warrior)
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(BluePrimaryDark, BluePrimary, BlueSecondary))).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = AccentOrange.copy(alpha = 0.3f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
                }
                Spacer(Modifier.height(16.dp))
                Text(user?.full_name ?: userText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(user?.email ?: "user@email.com", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = AccentGreen.copy(alpha = 0.3f)) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(ecoWarriorText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(user: UserDto?, appColors: AppColors) {
    val kgCo2SavedText = stringResource(R.string.kg_co2_saved)
    val totalTripsText = stringResource(R.string.total_trips)
    val dayStreakText = stringResource(R.string.day_streak_label)
    val pointsText = stringResource(R.string.points)
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(Icons.Default.Eco, "${user?.total_co2_saved?.toInt() ?: 0}", kgCo2SavedText, AccentGreen, appColors)
            StatItem(Icons.Default.Train, "${user?.total_trips ?: 0}", totalTripsText, AccentOrange, appColors)
            StatItem(Icons.Default.LocalFireDepartment, "${user?.streak_days ?: 0}", dayStreakText, AccentOrange, appColors)
            StatItem(Icons.Default.Star, "${user?.total_points ?: 0}", pointsText, AccentYellow, appColors)
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String, color: Color, appColors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = appColors.textPrimary)
        Text(label, fontSize = 10.sp, color = appColors.textSecondary)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, color: Color, appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = appColors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = appColors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
        }
    }
}
