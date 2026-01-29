package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.CarbonOffsetData
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarbonOffsetViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _offsetState = MutableStateFlow<UiState<CarbonOffsetData>>(UiState.Loading)
    val offsetState: StateFlow<UiState<CarbonOffsetData>> = _offsetState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getCarbonOffsetData().collect { _offsetState.value = it }
        }
    }
    
    fun contribute(projectId: String, amount: Float) {
        viewModelScope.launch {
            repository.contributeToProject(projectId, amount).collect { /* Handle result */ }
        }
    }
}
