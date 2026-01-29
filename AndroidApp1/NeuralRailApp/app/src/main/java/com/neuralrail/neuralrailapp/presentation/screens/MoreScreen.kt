package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.ViewModelFactory

data class FeatureItem(val route: String, val icon: ImageVector, val title: String, val description: String, val color: Color)

@Composable
fun MoreScreen(factory: ViewModelFactory, onNavigate: (String) -> Unit) {
    val appColors = LocalAppColors.current
    val energyTitle = stringResource(R.string.energy)
    val energyDesc = stringResource(R.string.realtime_train_energy)
    val bookTitle = stringResource(R.string.book_tickets)
    val bookDesc = stringResource(R.string.plan_eco_routes)
    val supportTitle = stringResource(R.string.support)
    val supportDesc = stringResource(R.string.support_desc)
    val learnTitle = stringResource(R.string.learn)
    val learnDesc = stringResource(R.string.rail_sustainability_facts)
    
    val features = listOf(
        FeatureItem("green_rail", Icons.Default.Bolt, energyTitle, energyDesc, AccentOrange),
        FeatureItem("planner", Icons.Default.ConfirmationNumber, bookTitle, bookDesc, BlueAccent),
        FeatureItem("carbon_offset", Icons.Default.Eco, supportTitle, supportDesc, AccentGreen),
        FeatureItem("education_hub", Icons.Default.School, learnTitle, learnDesc, AccentYellow),
    )

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apps, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.more_features), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.explore_eco_features), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(stringResource(R.string.explore_features), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp))
                Text(stringResource(R.string.discover_sustainable), fontSize = 13.sp, color = appColors.textSecondary)
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureCard(features[0], { onNavigate(features[0].route) }, Modifier.weight(1f), appColors)
                        FeatureCard(features[1], { onNavigate(features[1].route) }, Modifier.weight(1f), appColors)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureCard(features[2], { onNavigate(features[2].route) }, Modifier.weight(1f), appColors)
                        FeatureCard(features[3], { onNavigate(features[3].route) }, Modifier.weight(1f), appColors)
                    }
                }
            }
            
            item { Text(stringResource(R.string.about), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 8.dp)) }
            item { AboutCard(appColors) }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem, onClick: () -> Unit, modifier: Modifier = Modifier, appColors: AppColors) {
    Surface(
        modifier = modifier.height(130.dp).clickable(onClick = onClick), 
        shape = RoundedCornerShape(14.dp), 
        color = appColors.backgroundCard,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp), 
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp), 
                color = feature.color.copy(alpha = 0.2f), 
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    Icon(feature.icon, null, tint = feature.color, modifier = Modifier.size(24.dp)) 
                }
            }
            Column {
                Text(
                    feature.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = appColors.textPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    feature.description, 
                    fontSize = 11.sp, 
                    color = appColors.textSecondary, 
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AboutCard(appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = BluePrimary.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Train, null, tint = BluePrimary, modifier = Modifier.size(32.dp)) }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = appColors.textPrimary)
                    Text(stringResource(R.string.sustainable_travel_platform), fontSize = 13.sp, color = appColors.textSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.about_description), fontSize = 14.sp, color = appColors.textSecondary, lineHeight = 20.sp)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = appColors.divider)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.version), fontSize = 12.sp, color = appColors.textMuted)
                Text(stringResource(R.string.made_for_sustainable), fontSize = 12.sp, color = appColors.textMuted)
            }
        }
    }
}
