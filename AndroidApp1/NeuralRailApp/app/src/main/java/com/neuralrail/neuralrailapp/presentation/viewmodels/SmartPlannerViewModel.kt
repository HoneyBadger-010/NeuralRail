package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.SmartRoute
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartPlannerViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _routesState = MutableStateFlow<UiState<List<SmartRoute>>>(UiState.Loading)
    val routesState: StateFlow<UiState<List<SmartRoute>>> = _routesState.asStateFlow()
    
    init {
        searchRoutes("Central Station", "Tech Park")
    }
    
    fun searchRoutes(from: String, to: String) {
        viewModelScope.launch {
            repository.getSmartRoutes(from, to).collect { state ->
                _routesState.value = state
            }
        }
    }
}
