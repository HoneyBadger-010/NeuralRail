package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.models.WastageReport
import com.neuralrail.neuralrailapp.data.models.GeminiAnalysisResult
import com.neuralrail.neuralrailapp.data.repository.EnergyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReportUiState {
    data object Idle : ReportUiState()
    data object Analyzing : ReportUiState()
    data class Analyzed(val result: GeminiAnalysisResult) : ReportUiState()
    data object Submitting : ReportUiState()
    data object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

class ReportWastageViewModel(
    private val repository: EnergyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var currentDescription: String = ""
    private var currentLocation: String = ""

    fun analyzeReport(description: String, location: String) {
        if (description.isBlank() || location.isBlank()) {
            _uiState.value = ReportUiState.Error("Please fill in both fields.")
            return
        }
        
        currentDescription = description
        currentLocation = location
        
        viewModelScope.launch {
            _uiState.value = ReportUiState.Analyzing
            try {
                // Call Gemini for analysis
                val analysis = repository.analyzeReport(description, location)
                _uiState.value = ReportUiState.Analyzed(analysis)
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error("Analysis failed: ${e.message}")
            }
        }
    }

    fun submitReport(userId: String) {
        val currentState = _uiState.value
        if (currentState !is ReportUiState.Analyzed) return

        viewModelScope.launch {
            _uiState.value = ReportUiState.Submitting
            try {
                val report = WastageReport(
                    userId = userId,
                    description = currentDescription,
                    location = currentLocation,
                    timestamp = System.currentTimeMillis(),
                    aiAnalysis = currentState.result,
                    status = "PENDING"
                )
                
                val result = repository.submitReport(report)
                if (result.isSuccess) {
                    _uiState.value = ReportUiState.Success
                } else {
                    _uiState.value = ReportUiState.Error("Submission failed. Try again.")
                }
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }
}
