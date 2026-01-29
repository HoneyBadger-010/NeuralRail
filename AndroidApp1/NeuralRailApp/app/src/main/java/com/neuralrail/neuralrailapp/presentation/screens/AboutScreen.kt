package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.presentation.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Icon(Icons.Default.Info, null, tint = AccentOrange, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.about), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.learn_about_neuralrail), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { AppInfoCard(appColors) }
            item { MissionCard(appColors) }
            item { Text(stringResource(R.string.features), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { FeatureItem(Icons.Default.Train, stringResource(R.string.smart_travel), stringResource(R.string.smart_travel_desc), AccentOrange, appColors) }
            item { FeatureItem(Icons.Default.Eco, stringResource(R.string.carbon_tracking), stringResource(R.string.carbon_tracking_desc), AccentGreen, appColors) }
            item { FeatureItem(Icons.Default.EmojiEvents, stringResource(R.string.gamification), stringResource(R.string.gamification_desc), AccentYellow, appColors) }
            item { FeatureItem(Icons.Default.Visibility, stringResource(R.string.community_watch), stringResource(R.string.community_watch_feature_desc), AccentOrange, appColors) }
            item { Text(stringResource(R.string.legal), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { LegalItem(Icons.Default.Description, stringResource(R.string.terms_of_service), appColors) }
            item { LegalItem(Icons.Default.PrivacyTip, stringResource(R.string.privacy_policy), appColors) }
            item { LegalItem(Icons.Default.Gavel, stringResource(R.string.licenses), appColors) }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AppInfoCard(appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(BluePrimaryDark, BluePrimary, BlueSecondary))).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Train, null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.app_name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text(stringResource(R.string.sustainable_travel_platform), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Text(stringResource(R.string.version), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MissionCard(appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = appColors.backgroundCard) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, null, tint = AccentRed, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.our_mission), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = appColors.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.mission_description), fontSize = 14.sp, color = appColors.textSecondary, lineHeight = 22.sp, textAlign = TextAlign.Justify)
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String, color: Color, appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(26.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appColors.textPrimary)
                Text(description, fontSize = 13.sp, color = appColors.textSecondary)
            }
        }
    }
}

@Composable
private fun LegalItem(icon: ImageVector, title: String, appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = appColors.textPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
        }
    }
}
