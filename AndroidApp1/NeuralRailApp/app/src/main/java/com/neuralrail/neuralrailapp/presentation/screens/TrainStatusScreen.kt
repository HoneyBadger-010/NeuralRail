package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.TrainStatusViewModel

@Composable
fun TrainStatusScreen(viewModel: TrainStatusViewModel) {
    val liveTrainsState by viewModel.liveTrainsState.collectAsState()
    val trainStatusState by viewModel.trainStatusState.collectAsState()
    var trainNumber by remember { mutableStateOf("") }
    var showDetails by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Train, null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.train_status), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(stringResource(R.string.track_your_train), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                val enterTrainHint = stringResource(R.string.enter_train_number)
                Surface(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp), color = appColors.backgroundCard) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = BlueAccent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(
                            value = trainNumber, onValueChange = { trainNumber = it }, modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 15.sp, color = appColors.textPrimary), singleLine = true,
                            decorationBox = { inner -> if (trainNumber.isEmpty()) Text(enterTrainHint, color = appColors.textMuted, fontSize = 15.sp); inner() }
                        )
                        Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { viewModel.searchTrain(trainNumber); showDetails = true }, color = BlueAccent) {
                            Text(stringResource(R.string.track), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (showDetails) {
                item {
                    when (val state = trainStatusState) {
                        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) }
                        is UiState.Success -> TrainDetailCard(state.data)
                        is UiState.Error -> ErrorCard(state.message)
                    }
                }
            }
            
            item { Text(stringResource(R.string.live_trains), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)); Text(stringResource(R.string.trains_with_delays), fontSize = 13.sp, color = appColors.textSecondary) }
            
            when (val state = liveTrainsState) {
                is UiState.Loading -> item { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) } }
                is UiState.Success -> items(state.data.filter { it.currentStatus != TrainRunningStatus.ON_TIME }) { train -> LiveTrainCard(train, appColors) { viewModel.searchTrain(train.trainNumber); showDetails = true } }
                is UiState.Error -> item { ErrorCard(state.message) }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun TrainDetailCard(train: TrainStatus) {
    val statusColor = getStatusColor(train.currentStatus)
    val localAppColors = LocalAppColors.current
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = localAppColors.backgroundCard) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.8f), statusColor.copy(alpha = 0.5f)))).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Train, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(train.trainNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                            Text(train.currentStatus.name.replace("_", " "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Text(train.trainName, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(train.currentLocation, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                if (train.delay > 0) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = AccentRed.copy(alpha = 0.2f)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = AccentRed, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.delayed_by, train.delay), fontWeight = FontWeight.Bold, color = AccentRed, fontSize = 14.sp)
                                train.delayReason?.let { reason -> Text(getDelayReasonText(reason), fontSize = 12.sp, color = localAppColors.textSecondary) }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text(stringResource(R.string.next_station), fontSize = 12.sp, color = localAppColors.textSecondary); Text(train.nextStation, fontWeight = FontWeight.Medium, color = localAppColors.textPrimary) }
                    Column(horizontalAlignment = Alignment.End) { Text(stringResource(R.string.expected_arrival), fontSize = 12.sp, color = localAppColors.textSecondary); Text(train.expectedArrival, fontWeight = FontWeight.Medium, color = if (train.delay > 0) AccentRed else AccentGreen) }
                }
            }
        }
    }
}

@Composable
private fun LiveTrainCard(train: TrainStatus, appColors: AppColors, onClick: () -> Unit) {
    val statusColor = getStatusColor(train.currentStatus)
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(when (train.currentStatus) { TrainRunningStatus.STOPPED -> Icons.Default.PauseCircle; TrainRunningStatus.DELAYED -> Icons.Default.Schedule; else -> Icons.Default.Train }, null, tint = statusColor, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(train.trainNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appColors.textPrimary)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.2f)) {
                        Text(train.currentStatus.name.replace("_", " "), fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(train.trainName, fontSize = 13.sp, color = appColors.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = appColors.textMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(train.currentLocation, fontSize = 12.sp, color = appColors.textSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (train.delay > 0) Text("+${train.delay} min", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Icon(Icons.Default.ChevronRight, null, tint = appColors.textMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AccentRed.copy(alpha = 0.2f)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, color = AccentRed)
        }
    }
}

private fun getStatusColor(status: TrainRunningStatus): Color = when (status) {
    TrainRunningStatus.ON_TIME -> AccentGreen
    TrainRunningStatus.DELAYED -> AccentOrange
    TrainRunningStatus.STOPPED -> AccentRed
    TrainRunningStatus.CANCELLED -> AccentRed
    TrainRunningStatus.DIVERTED -> AccentOrange
    TrainRunningStatus.RESCHEDULED -> AccentCyan
}

@Composable
private fun getDelayReasonText(reason: DelayReason): String = when (reason) {
    DelayReason.SIGNAL_FAILURE -> stringResource(R.string.signal_failure)
    DelayReason.TRACK_MAINTENANCE -> stringResource(R.string.track_maintenance)
    DelayReason.WEATHER_CONDITIONS -> stringResource(R.string.weather_conditions)
    DelayReason.TECHNICAL_ISSUE -> stringResource(R.string.technical_issue)
    DelayReason.PASSENGER_EMERGENCY -> stringResource(R.string.passenger_emergency)
    DelayReason.SECURITY_CHECK -> stringResource(R.string.security_check)
    DelayReason.CONGESTION -> stringResource(R.string.track_congestion)
    DelayReason.ACCIDENT_AHEAD -> stringResource(R.string.accident_ahead)
    DelayReason.POWER_FAILURE -> stringResource(R.string.power_failure)
    DelayReason.CREW_CHANGE -> stringResource(R.string.crew_change)
    DelayReason.UNKNOWN -> stringResource(R.string.unknown)
}
