package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.data.models.GeminiAnalysisResult
import com.neuralrail.neuralrailapp.presentation.viewmodels.ReportUiState
import com.neuralrail.neuralrailapp.presentation.viewmodels.ReportWastageViewModel
import com.neuralrail.neuralrailapp.presentation.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportWastageScreen(
    viewModel: ReportWastageViewModel,
    onBack: () -> Unit,
    userId: String = "anonymous_user"
) {
    val uiState by viewModel.uiState.collectAsState()
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Energy Wastage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Text(
                "Help Indian Railways Save Energy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe the wastage") },
                placeholder = { Text("Ex: Lights ON in waiting room no. 2 with no passengers.") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Station Name") },
                placeholder = { Text("Ex: Pune Junction, Platform 1") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button based on State
            when (val state = uiState) {
                is ReportUiState.Idle -> {
                    Button(
                        onClick = { viewModel.analyzeReport(description, location) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = description.isNotBlank() && location.isNotBlank()
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Report")
                    }
                }
                
                is ReportUiState.Analyzing -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AI is validating your report...")
                }
                
                is ReportUiState.Analyzed -> {
                    AnalysisResultCard(state.result)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { viewModel.submitReport(userId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Submit Report")
                        }
                    }
                }
                
                is ReportUiState.Submitting -> {
                    CircularProgressIndicator()
                    Text("Submitting to database...")
                }
                
                is ReportUiState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(64.dp))
                        Text("Report Submitted Successfully!", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            viewModel.resetState()
                            description = ""
                            location = ""
                            onBack()
                        }) {
                            Text("Done")
                        }
                    }
                }
                
                is ReportUiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red)
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisResultCard(result: GeminiAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.isValid) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Classification: ${result.classification}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Priority: ${result.priority}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Explanation: ${result.explanation}")
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Blue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recommended Action: ${result.recommendedAction}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
