package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.EcoCommuteData
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EcoCommuteViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _ecoCommuteState = MutableStateFlow<UiState<EcoCommuteData>>(UiState.Loading)
    val ecoCommuteState: StateFlow<UiState<EcoCommuteData>> = _ecoCommuteState.asStateFlow()
    
    init {
        loadEcoCommuteData()
    }
    
    private fun loadEcoCommuteData() {
        viewModelScope.launch {
            repository.getEcoCommuteData().collect { state ->
                _ecoCommuteState.value = state
            }
        }
    }
}
