package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.presentation.viewmodels.LiveViewModel

@Composable
fun LiveScreen(viewModel: LiveViewModel) {
    val energyDataState by viewModel.energyDataState.collectAsState()

    AnimatedContent(targetState = energyDataState, label = "LiveScreen") {
        when (val state = it) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Train Energy Usage: ${state.data.energyUsage} kWh")
                    Text(text = "Regenerative Braking Recovery: ${state.data.regenerativeBrakingRecovery * 100}%")
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message)
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { viewModel.getEnergyData("train_123") }) {
            Text("Fetch Energy Data")
        }
    }
}