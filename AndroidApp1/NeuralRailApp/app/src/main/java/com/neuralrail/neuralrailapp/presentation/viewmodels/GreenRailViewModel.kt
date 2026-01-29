package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.CityEnergySummary
import com.neuralrail.neuralrailapp.data.models.RealTimeEnergyData
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GreenRailViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _realTimeEnergyState = MutableStateFlow<UiState<RealTimeEnergyData>>(UiState.Loading)
    val realTimeEnergyState: StateFlow<UiState<RealTimeEnergyData>> = _realTimeEnergyState.asStateFlow()
    
    private val _cityEnergyState = MutableStateFlow<UiState<CityEnergySummary>>(UiState.Loading)
    val cityEnergyState: StateFlow<UiState<CityEnergySummary>> = _cityEnergyState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getCityEnergySummary().collect { _cityEnergyState.value = it }
        }
        viewModelScope.launch {
            repository.getRealTimeEnergy("train_12045").collect { _realTimeEnergyState.value = it }
        }
    }
}
