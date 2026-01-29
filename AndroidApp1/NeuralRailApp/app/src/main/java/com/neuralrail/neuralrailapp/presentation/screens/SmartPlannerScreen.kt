package com.neuralrail.neuralrailapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.SmartRoute
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.SmartPlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlannerScreen(viewModel: SmartPlannerViewModel, onBack: () -> Unit = {}) {
    val routesState by viewModel.routesState.collectAsState()
    var fromStation by remember { mutableStateOf("") }
    var toStation by remember { mutableStateOf("") }
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val availableRoutesText = stringResource(R.string.available_routes)
    val routeSelectedText = stringResource(R.string.route_selected)

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header
        PlannerHeader(onBack = onBack)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Card
            item {
                SearchCard(
                    fromStation = fromStation,
                    toStation = toStation,
                    onFromChange = { fromStation = it },
                    onToChange = { toStation = it },
                    onSwap = { 
                        val temp = fromStation
                        fromStation = toStation
                        toStation = temp 
                    },
                    onSearch = { 
                        viewModel.searchRoutes(
                            fromStation.ifEmpty { "Central" }, 
                            toStation.ifEmpty { "Tech Park" }
                        ) 
                    }
                )
            }
            
            // Eco Info Banner
            item { EcoInfoBanner() }
            
            // Routes Section
            when (val state = routesState) {
                is UiState.Loading -> {
                    item { LoadingCard() }
                }
                is UiState.Success -> {
                    if (state.data.isNotEmpty()) {
                        item {
                            Text(
                                availableRoutesText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = LocalAppColors.current.textPrimary
                            )
                        }
                        items(state.data) { route ->
                            RouteCard(route) {
                                Toast.makeText(
                                    context,
                                    routeSelectedText.format(route.from, route.to),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    item { ErrorCard(state.message) }
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PlannerHeader(onBack: () -> Unit) {
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
                    Icon(Icons.Default.ConfirmationNumber, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.book_ticket),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.find_eco_routes),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SearchCard(
    fromStation: String,
    toStation: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
    onSearch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = BlueAccent.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Route, null, tint = BlueAccent, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.plan_your_journey),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        stringResource(R.string.find_most_eco_route),
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // From/To Input Section
            Row(modifier = Modifier.fillMaxWidth()) {
                // Timeline dots
                Column(
                    modifier = Modifier.padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = AccentGreen
                    ) {}
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .background(DividerColor)
                    )
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = AccentRed
                    ) {}
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Input fields
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = fromStation,
                        onValueChange = onFromChange,
                        label = { Text(stringResource(R.string.from), color = LocalAppColors.current.textSecondary) },
                        placeholder = { Text(stringResource(R.string.enter_departure), color = LocalAppColors.current.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            cursorColor = BlueAccent
                        )
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = toStation,
                        onValueChange = onToChange,
                        label = { Text(stringResource(R.string.to), color = LocalAppColors.current.textSecondary) },
                        placeholder = { Text(stringResource(R.string.enter_destination), color = LocalAppColors.current.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            cursorColor = BlueAccent
                        )
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                // Swap button
                Surface(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSwap),
                    shape = CircleShape,
                    color = BlueAccent.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SwapVert,
                            null,
                            tint = BlueAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Search Button
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.find_routes), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun EcoInfoBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AccentGreen.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = AccentGreen.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Eco, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.eco_smart_routing),
                    fontSize = 14.sp,
                    color = AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.routes_sorted_carbon),
                    fontSize = 12.sp,
                    color = AccentGreen.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
private fun RouteCard(route: SmartRoute, onSelect: () -> Unit) {
    val isGreen = route.isGreenRecommended
    val accentColor = if (isGreen) AccentGreen else BlueAccent
    val greenRecommendedText = stringResource(R.string.green_recommended)
    val standardRouteText = stringResource(R.string.standard_route)
    val durationText = stringResource(R.string.duration)
    val distanceText = stringResource(R.string.distance)
    val minText = stringResource(R.string.min)
    val kmText = stringResource(R.string.km)
    val kgText = stringResource(R.string.kg)
    val co2Text = stringResource(R.string.co2)
    val trafficText = stringResource(R.string.traffic)
    val selectText = stringResource(R.string.select)
    val lowText = stringResource(R.string.low)
    val highText = stringResource(R.string.high)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isGreen) Icons.Default.Eco else Icons.Default.Train,
                                null,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${route.from} → ${route.to}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = LocalAppColors.current.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (isGreen) "🌿 $greenRecommendedText" else standardRouteText,
                            fontSize = 13.sp,
                            color = if (isGreen) AccentGreen else TextSecondary
                        )
                    }
                }
                
                // Efficiency Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            null,
                            tint = AccentGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${(route.energyEfficiencyScore * 100).toInt()}%",
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            val localAppColors = LocalAppColors.current
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = localAppColors.backgroundCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RouteStatItem(
                        icon = Icons.Default.Schedule,
                        value = "${route.duration}",
                        unit = minText,
                        label = durationText
                    )
                    VerticalDivider(Modifier.height(40.dp), color = DividerColor)
                    RouteStatItem(
                        icon = Icons.Default.Straighten,
                        value = "${route.distance.toInt()}",
                        unit = kmText,
                        label = distanceText
                    )
                    VerticalDivider(Modifier.height(40.dp), color = DividerColor)
                    RouteStatItem(
                        icon = Icons.Default.Cloud,
                        value = "-${route.carbonFootprint.toInt()}",
                        unit = kgText,
                        label = co2Text
                    )
                }
            }
            
            Spacer(Modifier.height(14.dp))
            
            // Traffic indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val trafficColor = when (route.congestionLevel.name) {
                        "LOW" -> AccentGreen
                        "MEDIUM" -> AccentYellow
                        else -> AccentOrange
                    }
                    val congestionText = when (route.congestionLevel.name) {
                        "LOW" -> lowText
                        else -> highText
                    }
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = trafficColor
                    ) {}
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$trafficText: $congestionText",
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                }
                
                // Select Button
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteStatItem(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            null,
            tint = BlueAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LocalAppColors.current.textPrimary
            )
            Spacer(Modifier.width(2.dp))
            Text(
                unit,
                fontSize = 11.sp,
                color = LocalAppColors.current.textSecondary,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
        Text(
            label,
            fontSize = 11.sp,
            color = LocalAppColors.current.textMuted
        )
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = BlueAccent)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.finding_routes),
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AccentRed.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = AccentRed.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Error,
                        null,
                        tint = AccentRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                color = AccentRed,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
