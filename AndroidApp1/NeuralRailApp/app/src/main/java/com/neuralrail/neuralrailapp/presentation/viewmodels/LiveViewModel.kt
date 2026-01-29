package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.EnergyData
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.domain.GetEnergyDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LiveViewModel(private val getEnergyDataUseCase: GetEnergyDataUseCase) : ViewModel() {

    private val _energyDataState = MutableStateFlow<UiState<EnergyData>>(UiState.Loading)
    val energyDataState: StateFlow<UiState<EnergyData>> = _energyDataState.asStateFlow()

    fun getEnergyData(trainId: String) {
        getEnergyDataUseCase(trainId)
            .onEach { _energyDataState.value = it }
            .launchIn(viewModelScope)
    }
}