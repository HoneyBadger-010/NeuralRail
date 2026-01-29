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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.CityEnergySummary
import com.neuralrail.neuralrailapp.data.models.RealTimeEnergyData
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.GreenRailViewModel

@Composable
fun GreenRailScreen(viewModel: GreenRailViewModel, onBack: () -> Unit = {}) {
    val energyState by viewModel.realTimeEnergyState.collectAsState()
    val cityState by viewModel.cityEnergyState.collectAsState()
    val appColors = LocalAppColors.current
    val energyMetricsText = stringResource(R.string.energy_metrics)

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Icon(Icons.Default.Bolt, null, tint = AccentOrange, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.live_energy), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.realtime_energy_monitoring), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { when (val energy = energyState) { is UiState.Success -> ModernRealTimeCard(energy.data, appColors); is UiState.Loading -> LoadingCard(appColors); is UiState.Error -> ErrorCard(energy.message) } }
            item { Text(energyMetricsText, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { when (val energy = energyState) { is UiState.Success -> ModernEnergyMetrics(energy.data, appColors); else -> {} } }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ModernCityEnergyCard(data: CityEnergySummary) {
    val energyDashboardText = stringResource(R.string.energy_dashboard, data.cityName)
    val trainsRenewableText = stringResource(R.string.trains_renewable, data.totalTrainsRunning, (data.renewablePoweredPercent * 100).toInt())
    val kwhSavedText = stringResource(R.string.kwh_saved_today)
    
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(BluePrimaryDark, BluePrimary, BlueSecondary))).padding(20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationCity, null, tint = AccentOrange, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(energyDashboardText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(trainsRenewableText, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${data.totalEnergySaved.toInt()}", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Text(" $kwhSavedText", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernRealTimeCard(data: RealTimeEnergyData, appColors: AppColors) {
    val trainIdText = stringResource(R.string.train_id, data.trainId)
    val liveText = stringResource(R.string.live)
    val usageText = stringResource(R.string.usage)
    val regenText = stringResource(R.string.regen)
    val renewableText = stringResource(R.string.renewable)
    
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = appColors.backgroundCard) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Train, null, tint = AccentOrange, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Column { Text(data.trainName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appColors.textPrimary); Text(trainIdText, fontSize = 12.sp, color = appColors.textSecondary) }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = AccentGreen.copy(alpha = 0.2f)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FiberManualRecord, null, tint = AccentRed, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(liveText, color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                EnergyStatItem(Icons.Default.Bolt, "${data.currentEnergyUsage.toInt()}", "kWh", usageText, AccentOrange, appColors)
                EnergyStatItem(Icons.Default.Autorenew, "${(data.regenerativeBrakingRecovery * 100).toInt()}%", "", regenText, AccentGreen, appColors)
                EnergyStatItem(Icons.Default.WbSunny, "${(data.renewableEnergyPercent * 100).toInt()}%", "", renewableText, AccentYellow, appColors)
            }
        }
    }
}

@Composable
private fun EnergyStatItem(icon: ImageVector, value: String, unit: String, label: String, color: Color, appColors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) { Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color); if (unit.isNotEmpty()) Text(unit, fontSize = 12.sp, color = appColors.textSecondary) }
        Text(label, fontSize = 12.sp, color = appColors.textSecondary)
    }
}

@Composable
private fun ModernEnergyMetrics(data: RealTimeEnergyData, appColors: AppColors) {
    val regenerativeBrakingText = stringResource(R.string.regenerative_braking)
    val renewableEnergyText = stringResource(R.string.renewable_energy)
    val efficiencyScoreText = stringResource(R.string.efficiency_score)
    
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = appColors.backgroundCard) {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricProgressItem(Icons.Default.Autorenew, regenerativeBrakingText, data.regenerativeBrakingRecovery, AccentGreen, appColors)
            Spacer(Modifier.height(16.dp))
            MetricProgressItem(Icons.Default.WbSunny, renewableEnergyText, data.renewableEnergyPercent, AccentYellow, appColors)
            Spacer(Modifier.height(16.dp))
            MetricProgressItem(Icons.Default.Speed, efficiencyScoreText, 0.85f, AccentOrange, appColors)
        }
    }
}

@Composable
private fun MetricProgressItem(icon: ImageVector, label: String, progress: Float, color: Color, appColors: AppColors) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textPrimary) }
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = color, trackColor = color.copy(alpha = 0.15f))
    }
}

@Composable
private fun LoadingCard(appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp), color = appColors.backgroundCard) {
        Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentOrange) }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AccentRed.copy(alpha = 0.2f)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text(message, color = AccentRed)
        }
    }
}
