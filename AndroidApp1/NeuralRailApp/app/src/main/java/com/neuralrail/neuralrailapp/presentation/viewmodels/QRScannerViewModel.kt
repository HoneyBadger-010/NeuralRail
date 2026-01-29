package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.QRScanResult
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QRScannerViewModel(private val repository: EcoRepository) : ViewModel() {
    
    private val _scanResultState = MutableStateFlow<UiState<QRScanResult>?>(null)
    val scanResultState: StateFlow<UiState<QRScanResult>?> = _scanResultState.asStateFlow()
    
    fun processQRCode(rawValue: String) {
        viewModelScope.launch {
            repository.parseQRCode(rawValue).collect { _scanResultState.value = it }
        }
    }
    
    fun clearResult() {
        _scanResultState.value = null
    }
}
