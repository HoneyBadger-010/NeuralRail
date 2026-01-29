package com.neuralrail.neuralrailapp.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*

@Composable
fun EcoStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(GreenPrimary, GreenSecondary)
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Icon(icon, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
            }
        }
    }
}

@Composable
fun ProgressCard(
    title: String,
    current: Float,
    goal: Float,
    unit: String,
    modifier: Modifier = Modifier
) {
    val progress = (current / goal).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, tween(1000), label = "progress")
    
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Medium)
                Text("${current.toInt()}/${goal.toInt()} $unit", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = GreenPrimary,
                trackColor = GreenPrimary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, modifier: Modifier = Modifier) {
    val badgeColor = when (badge.iconType) {
        BadgeType.BRONZE -> BronzeBadge
        BadgeType.SILVER -> SilverBadge
        BadgeType.GOLD -> GoldBadge
        BadgeType.PLATINUM -> PlatinumBadge
        BadgeType.SPECIAL -> GreenAccent
    }
    
    Column(modifier = modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (badge.isUnlocked) badgeColor else Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (badge.isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                null,
                tint = if (badge.isUnlocked) Color.White else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(badge.name, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2)
        if (!badge.isUnlocked && badge.progress > 0) {
            Text("${(badge.progress * 100).toInt()}%", fontSize = 10.sp, color = GreenPrimary)
        }
    }
}


@Composable
fun TripCard(trip: EcoTrip, modifier: Modifier = Modifier) {
    val modeIcon = when (trip.mode) {
        TransportMode.RAIL -> Icons.Default.Train
        TransportMode.METRO -> Icons.Default.Subway
        TransportMode.BUS -> Icons.Default.DirectionsBus
        TransportMode.WALK -> Icons.Default.DirectionsWalk
        TransportMode.CYCLE -> Icons.Default.PedalBike
    }
    
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(GreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(modeIcon, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${trip.from} → ${trip.to}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(trip.date, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-${trip.co2Saved}kg", color = GreenPrimary, fontWeight = FontWeight.Bold)
                Text("CO₂", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RouteCard(route: SmartRoute, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val congestionColor = when (route.congestionLevel) {
        CongestionLevel.LOW -> GreenPrimary
        CongestionLevel.MEDIUM -> WarningOrange
        CongestionLevel.HIGH -> ErrorRed
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = if (route.isGreenRecommended) CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.05f)) else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (route.isGreenRecommended) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Eco, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Recommended", color = GreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${route.duration} min", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${route.distance} km", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${route.carbonFootprint}kg CO₂", color = if (route.isGreenRecommended) GreenPrimary else Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(congestionColor))
                        Spacer(Modifier.width(4.dp))
                        Text(route.congestionLevel.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp)
                    }
                }
            }
            if (route.renewableStations.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, null, tint = SolarYellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Via: ${route.renewableStations.joinToString(", ")}", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSelect, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                Text("Select Route")
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: DailyChallenge, onComplete: () -> Unit, modifier: Modifier = Modifier) {
    val typeIcon = when (challenge.type) {
        ChallengeType.TRAVEL -> Icons.Default.Train
        ChallengeType.WALK -> Icons.Default.DirectionsWalk
        ChallengeType.OFF_PEAK -> Icons.Default.Schedule
        ChallengeType.SHARE -> Icons.Default.Share
        ChallengeType.REPORT -> Icons.Default.Report
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = if (challenge.isCompleted) CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f)) else CardDefaults.cardColors()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(if (challenge.isCompleted) GreenPrimary else GreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (challenge.isCompleted) Icons.Default.Check else typeIcon,
                    null,
                    tint = if (challenge.isCompleted) Color.White else GreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.title, fontWeight = FontWeight.Medium)
                Text(challenge.description, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+${challenge.points}", color = GoldBadge, fontWeight = FontWeight.Bold)
                if (!challenge.isCompleted) {
                    TextButton(onClick = onComplete, contentPadding = PaddingValues(0.dp)) {
                        Text("Complete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyGauge(value: Float, maxValue: Float, label: String, modifier: Modifier = Modifier) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, tween(1000), label = "gauge")
    
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(80.dp),
                strokeWidth = 8.dp,
                color = GreenPrimary,
                trackColor = GreenPrimary.copy(alpha = 0.2f)
            )
            Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun ReportCard(report: EnergyReport, onUpvote: () -> Unit, modifier: Modifier = Modifier) {
    val statusColor = when (report.status) {
        ReportStatus.PENDING -> WarningOrange
        ReportStatus.INVESTIGATING -> EnergyBlue
        ReportStatus.RESOLVED -> GreenPrimary
    }
    val typeIcon = when (report.type) {
        ReportType.LIGHTS_ON -> Icons.Default.Lightbulb
        ReportType.IDLING_ENGINE -> Icons.Default.Train
        ReportType.FAULTY_SOLAR -> Icons.Default.WbSunny
        ReportType.AC_WASTE -> Icons.Default.AcUnit
        ReportType.OTHER -> Icons.Default.Report
    }
    
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(typeIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(report.location, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(report.status.name, fontSize = 10.sp, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(report.description, fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.timestamp, fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onUpvote, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${report.upvotes}")
                }
            }
        }
    }
}

@Composable
fun OffsetProjectCard(project: OffsetProject, onContribute: () -> Unit, modifier: Modifier = Modifier) {
    val progress = (project.currentAmount / project.targetAmount).coerceIn(0f, 1f)
    val typeIcon = when (project.type) {
        ProjectType.SOLAR -> Icons.Default.WbSunny
        ProjectType.REFORESTATION -> Icons.Default.Forest
        ProjectType.EV_CHARGING -> Icons.Default.EvStation
        ProjectType.WIND -> Icons.Default.Air
    }
    val typeColor = when (project.type) {
        ProjectType.SOLAR -> SolarYellow
        ProjectType.REFORESTATION -> GreenPrimary
        ProjectType.EV_CHARGING -> EnergyBlue
        ProjectType.WIND -> CarbonGray
    }
    
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, fontWeight = FontWeight.Medium)
                    Text(project.impactPerUnit, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(project.description, fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = typeColor,
                trackColor = typeColor.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(4.dp))
            Text("₹${project.currentAmount.toInt()} / ₹${project.targetAmount.toInt()}", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onContribute, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = typeColor)) {
                Text("Contribute")
            }
        }
    }
}
