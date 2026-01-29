package com.neuralrail.neuralrailapp.presentation.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.CommunityWatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityWatchScreen(viewModel: CommunityWatchViewModel, onBack: () -> Unit = {}) {
    val reportsState by viewModel.reportsState.collectAsState()
    val statsState by viewModel.communityStatsState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    val allText = stringResource(R.string.all)
    val pendingText = stringResource(R.string.pending)
    val investigatingText = stringResource(R.string.investigating)
    val resolvedText = stringResource(R.string.resolved)
    var selectedFilter by remember { mutableStateOf(allText) }
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        CommunityHeader(onBack = onBack)
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (val stats = statsState) {
                        is UiState.Success -> StatsCard(stats.data)
                        is UiState.Loading -> LoadingCard()
                        is UiState.Error -> ErrorCard(stats.message)
                    }
                }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.quick_report),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LocalAppColors.current.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.tap_to_report),
                            fontSize = 13.sp,
                            color = LocalAppColors.current.textSecondary
                        )
                    }
                }
                
                item { QuickReportGrid { showReportDialog = true } }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.recent_reports),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LocalAppColors.current.textPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val filters = listOf(allText, pendingText, investigatingText, resolvedText)
                            items(filters) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { 
                                        Text(
                                            filter, 
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Normal
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = LocalAppColors.current.backgroundCard,
                                        labelColor = LocalAppColors.current.textSecondary,
                                        selectedContainerColor = AccentOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
                
                when (val reports = reportsState) {
                    is UiState.Success -> {
                        val filtered = if (selectedFilter == allText) reports.data
                        else reports.data.filter { it.status.name.equals(selectedFilter, ignoreCase = true) }
                        
                        if (filtered.isEmpty()) {
                            item { EmptyCard() }
                        } else {
                            items(filtered) { report ->
                                val upvotedMsg = stringResource(R.string.upvoted_report)
                                ReportCard(report) {
                                    Toast.makeText(context, upvotedMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    is UiState.Loading -> item { LoadingCard() }
                    is UiState.Error -> item { ErrorCard(reports.message) }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
            
            ExtendedFloatingActionButton(
                onClick = { showReportDialog = true },
                containerColor = AccentOrange,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.report_issue), fontWeight = FontWeight.SemiBold) }
            )
        }
    }
    
    val reportSubmittedMsg = stringResource(R.string.report_submitted)
    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { type, location, desc ->
                viewModel.submitReport(EnergyReport("", type, location, desc, ReportStatus.PENDING, "Just now", 0))
                showReportDialog = false
                Toast.makeText(context, reportSubmittedMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun CommunityHeader(onBack: () -> Unit) {
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
                    Icon(Icons.Default.ReportProblem, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.report_problem),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.report_and_track),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatsCard(stats: CommunityStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Public, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            stringResource(R.string.community_impact),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            stringResource(R.string.together_making_difference),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(Icons.Default.Description, "${stats.totalReports}", stringResource(R.string.reports))
                    StatItem(Icons.Default.CheckCircle, "${stats.resolvedReports}", stringResource(R.string.resolved))
                    StatItem(Icons.Default.Bolt, "${stats.energySavedFromReports.toInt()}", stringResource(R.string.kwh))
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun QuickReportGrid(onReportClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickReportItem(Icons.Default.Lightbulb, stringResource(R.string.lights), AccentYellow, Modifier.weight(1f), onReportClick)
        QuickReportItem(Icons.Default.AcUnit, stringResource(R.string.ac), AccentCyan, Modifier.weight(1f), onReportClick)
        QuickReportItem(Icons.Default.WbSunny, stringResource(R.string.solar), AccentOrange, Modifier.weight(1f), onReportClick)
        QuickReportItem(Icons.Default.Train, stringResource(R.string.engine), BlueAccent, Modifier.weight(1f), onReportClick)
    }
}

@Composable
private fun QuickReportItem(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(95.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.backgroundCard),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = LocalAppColors.current.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun ReportCard(report: EnergyReport, onUpvote: () -> Unit) {
    val statusColor = when (report.status) {
        ReportStatus.PENDING -> AccentYellow
        ReportStatus.INVESTIGATING -> BlueAccent
        ReportStatus.RESOLVED -> AccentGreen
    }
    val typeIcon = when (report.type) {
        ReportType.LIGHTS_ON -> Icons.Default.Lightbulb
        ReportType.AC_WASTE -> Icons.Default.AcUnit
        ReportType.FAULTY_SOLAR -> Icons.Default.WbSunny
        ReportType.IDLING_ENGINE -> Icons.Default.Train
        ReportType.OTHER -> Icons.Default.Report
    }
    val typeColor = when (report.type) {
        ReportType.LIGHTS_ON -> AccentYellow
        ReportType.AC_WASTE -> AccentCyan
        ReportType.FAULTY_SOLAR -> AccentOrange
        ReportType.IDLING_ENGINE -> BlueAccent
        ReportType.OTHER -> TextSecondary
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            report.type.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = LocalAppColors.current.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = LocalAppColors.current.textSecondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                report.location,
                                fontSize = 13.sp,
                                color = LocalAppColors.current.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        report.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }
            
            if (report.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    report.description,
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    report.timestamp,
                    fontSize = 12.sp,
                    color = LocalAppColors.current.textMuted
                )
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onUpvote),
                    shape = RoundedCornerShape(8.dp),
                    color = BlueAccent.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            null,
                            tint = BlueAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${report.upvotes}",
                            fontSize = 13.sp,
                            color = BlueAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDialog(onDismiss: () -> Unit, onSubmit: (ReportType, String, String) -> Unit) {
    var selectedType by remember { mutableStateOf(ReportType.LIGHTS_ON) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LocalAppColors.current.backgroundCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AccentOrange.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReportProblem, null, tint = AccentOrange, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.report_energy_waste), fontWeight = FontWeight.Bold, color = LocalAppColors.current.textPrimary)
                    Text(stringResource(R.string.help_save_resources), fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
                }
            }
        },
        text = {
            Column {
                Text(stringResource(R.string.issue_type), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = LocalAppColors.current.textPrimary)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TypeChip(Icons.Default.Lightbulb, stringResource(R.string.lights), AccentYellow, selectedType == ReportType.LIGHTS_ON, { selectedType = ReportType.LIGHTS_ON }, Modifier.weight(1f))
                        TypeChip(Icons.Default.AcUnit, stringResource(R.string.ac), AccentCyan, selectedType == ReportType.AC_WASTE, { selectedType = ReportType.AC_WASTE }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TypeChip(Icons.Default.WbSunny, stringResource(R.string.solar), AccentOrange, selectedType == ReportType.FAULTY_SOLAR, { selectedType = ReportType.FAULTY_SOLAR }, Modifier.weight(1f))
                        TypeChip(Icons.Default.Train, stringResource(R.string.engine), BlueAccent, selectedType == ReportType.IDLING_ENGINE, { selectedType = ReportType.IDLING_ENGINE }, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location_hint), color = LocalAppColors.current.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = BlueAccent) },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_optional), color = LocalAppColors.current.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Description, null, tint = BlueAccent) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
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
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, location, description) },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                enabled = location.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.submit), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = LocalAppColors.current.textSecondary)
            }
        }
    )
}

@Composable
private fun TypeChip(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val localAppColors = LocalAppColors.current
    val bgColor by animateColorAsState(
        if (selected) accentColor else localAppColors.backgroundCard,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        if (selected) Color.White else TextSecondary,
        label = "content"
    )
    
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = BlueAccent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, null, tint = BlueAccent, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.no_reports_found),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = LocalAppColors.current.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.be_first_report),
                fontSize = 14.sp,
                color = LocalAppColors.current.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlueAccent)
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
                    Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(22.dp))
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
