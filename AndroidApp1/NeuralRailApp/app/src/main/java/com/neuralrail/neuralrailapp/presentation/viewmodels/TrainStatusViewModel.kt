package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.TrainStatus
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainStatusViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _trainStatusState = MutableStateFlow<UiState<TrainStatus>>(UiState.Loading)
    val trainStatusState: StateFlow<UiState<TrainStatus>> = _trainStatusState.asStateFlow()
    
    private val _liveTrainsState = MutableStateFlow<UiState<List<TrainStatus>>>(UiState.Loading)
    val liveTrainsState: StateFlow<UiState<List<TrainStatus>>> = _liveTrainsState.asStateFlow()
    
    init {
        loadLiveTrains()
    }
    
    fun loadLiveTrains() {
        viewModelScope.launch {
            repository.getLiveTrains().collect { _liveTrainsState.value = it }
        }
    }
    
    fun searchTrain(trainNumber: String) {
        viewModelScope.launch {
            repository.getTrainStatus(trainNumber).collect { _trainStatusState.value = it }
        }
    }
}
