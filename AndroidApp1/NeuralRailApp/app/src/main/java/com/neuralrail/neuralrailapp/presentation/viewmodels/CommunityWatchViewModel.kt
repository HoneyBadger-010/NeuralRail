package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.CommunityStats
import com.neuralrail.neuralrailapp.data.models.EnergyReport
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityWatchViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _reportsState = MutableStateFlow<UiState<List<EnergyReport>>>(UiState.Loading)
    val reportsState: StateFlow<UiState<List<EnergyReport>>> = _reportsState.asStateFlow()
    
    private val _communityStatsState = MutableStateFlow<UiState<CommunityStats>>(UiState.Loading)
    val communityStatsState: StateFlow<UiState<CommunityStats>> = _communityStatsState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getReports().collect { _reportsState.value = it }
        }
        viewModelScope.launch {
            repository.getCommunityStats().collect { _communityStatsState.value = it }
        }
    }
    
    fun submitReport(report: EnergyReport) {
        viewModelScope.launch {
            repository.submitReport(report).collect { /* Refresh data */ }
        }
    }
}
