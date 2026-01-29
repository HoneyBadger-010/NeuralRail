package com.neuralrail.neuralrailapp.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.neuralrail.neuralrailapp.data.models.CarbonOffsetData
import com.neuralrail.neuralrailapp.data.models.OffsetProject
import com.neuralrail.neuralrailapp.data.models.ProjectType
import com.neuralrail.neuralrailapp.data.models.UserContribution
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.CarbonOffsetViewModel

@Composable
fun CarbonOffsetScreen(viewModel: CarbonOffsetViewModel, onBack: () -> Unit = {}) {
    val state by viewModel.offsetState.collectAsState()
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Icon(Icons.Default.Eco, null, tint = AccentGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.support), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.support_green_initiatives), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        when (val currentState = state) {
            is UiState.Loading -> LoadingContent()
            is UiState.Success -> OffsetContent(currentState.data, viewModel, context)
            is UiState.Error -> ErrorContent(currentState.message)
        }
    }
}


@Composable
private fun OffsetContent(data: CarbonOffsetData, viewModel: CarbonOffsetViewModel, context: android.content.Context) {
    val offsetProjectsText = stringResource(R.string.offset_projects)
    val contributeCarbonNeutralText = stringResource(R.string.contribute_carbon_neutral)
    val yourContributionsText = stringResource(R.string.your_contributions)
    val thankYouText = stringResource(R.string.thank_you_contributed)
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CarbonBalanceCard(data) }
        
        item {
            Column {
                Text(offsetProjectsText, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalAppColors.current.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(contributeCarbonNeutralText, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
            }
        }
        
        items(data.availableProjects) { project ->
            ProjectCard(project) {
                viewModel.contribute(project.id, 10f)
                Toast.makeText(context, thankYouText.format(project.name), Toast.LENGTH_SHORT).show()
            }
        }
        
        if (data.userContributions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(yourContributionsText, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalAppColors.current.textPrimary)
            }
            items(data.userContributions) { contribution ->
                ContributionCard(contribution)
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun CarbonBalanceCard(data: CarbonOffsetData) {
    val remaining = (data.totalEmissions - data.offsetContributions).coerceAtLeast(0f).toInt()
    val progress = if (data.totalEmissions > 0) (data.offsetContributions / data.totalEmissions).coerceIn(0f, 1f) else 0f
    val yourCarbonBalanceText = stringResource(R.string.your_carbon_balance)
    val remainingToOffsetText = stringResource(R.string.remaining_to_offset)
    val percentOffsetText = stringResource(R.string.percent_offset, (progress * 100).toInt())
    val totalEmissionsText = stringResource(R.string.total_emissions)
    val offsetText = stringResource(R.string.offset)
    val treesEquivText = stringResource(R.string.trees_equiv)
    val kgText = stringResource(R.string.kg)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BluePrimaryDark, BluePrimary, BlueSecondary)))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, null, tint = AccentGreen, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(yourCarbonBalanceText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                Spacer(Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$remaining", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("$kgText CO₂", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
                Text(remainingToOffsetText, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                
                Spacer(Modifier.height(20.dp))
                
                // Progress bar
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(percentOffsetText, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = AccentGreen,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Stats row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatColumn(totalEmissionsText, "${data.totalEmissions.toInt()} $kgText", Icons.Default.Cloud)
                    StatColumn(offsetText, "${data.offsetContributions.toInt()} $kgText", Icons.Default.CheckCircle)
                    StatColumn(treesEquivText, "${(data.offsetContributions / 21).toInt()}", Icons.Default.Park)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProjectCard(project: OffsetProject, onContribute: () -> Unit) {
    val projectIcon = when (project.type) {
        ProjectType.REFORESTATION -> Icons.Default.Park
        ProjectType.SOLAR -> Icons.Default.WbSunny
        ProjectType.WIND -> Icons.Default.Air
        ProjectType.EV_CHARGING -> Icons.Default.EvStation
    }
    val projectColor = when (project.type) {
        ProjectType.REFORESTATION -> AccentGreen
        ProjectType.SOLAR -> AccentYellow
        ProjectType.WIND -> AccentCyan
        ProjectType.EV_CHARGING -> BlueAccent
    }
    val progress = if (project.targetAmount > 0) (project.currentAmount / project.targetAmount).coerceIn(0f, 1f) else 0f
    val raisedText = stringResource(R.string.raised, project.currentAmount.toInt())
    val goalText = stringResource(R.string.goal, project.targetAmount.toInt())
    val percentFundedText = stringResource(R.string.percent_funded, (progress * 100).toInt())
    val contributeNowText = stringResource(R.string.contribute_now)
    
    // Button animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_scale"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = projectColor.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(projectIcon, null, tint = projectColor, modifier = Modifier.size(30.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LocalAppColors.current.textPrimary)
                        Spacer(Modifier.height(2.dp))
                        Text(project.type.name.replace("_", " "), fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = projectColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        project.impactPerUnit,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = projectColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(Modifier.height(14.dp))
            
            // Description
            Text(
                project.description,
                fontSize = 14.sp,
                color = LocalAppColors.current.textSecondary,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Progress section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(raisedText, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
                Text(goalText, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = projectColor,
                trackColor = projectColor.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                percentFundedText,
                fontSize = 12.sp,
                color = projectColor,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Contribute button - Always green
            Button(
                onClick = onContribute,
                modifier = Modifier.fillMaxWidth().height(50.dp).scale(buttonScale),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                interactionSource = interactionSource
            ) {
                Icon(Icons.Default.VolunteerActivism, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(contributeNowText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ContributionCard(contribution: UserContribution) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = LocalAppColors.current.backgroundCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = AccentGreen.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contribution.impactDescription, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = LocalAppColors.current.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(contribution.date, fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
            }
            Text("₹${contribution.amount.toInt()}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentGreen, strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(16.dp), color = AccentRed.copy(alpha = 0.15f)) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Text(message, color = AccentRed, fontSize = 15.sp)
            }
        }
    }
}
