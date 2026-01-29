package com.neuralrail.neuralrailapp.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neuralrail.neuralrailapp.data.repository.EcoRepository

class ViewModelFactory(
    private val ecoRepository: EcoRepository,
    private val energyRepository: com.neuralrail.neuralrailapp.data.repository.EnergyRepository, // Add this
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EcoCommuteViewModel::class.java) -> {
                EcoCommuteViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(GreenRailViewModel::class.java) -> {
                GreenRailViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(SmartPlannerViewModel::class.java) -> {
                SmartPlannerViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(CarbonOffsetViewModel::class.java) -> {
                CarbonOffsetViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(GreenChallengeViewModel::class.java) -> {
                GreenChallengeViewModel(ecoRepository, context) as T
            }
            modelClass.isAssignableFrom(CommunityWatchViewModel::class.java) -> {
                CommunityWatchViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(EducationHubViewModel::class.java) -> {
                EducationHubViewModel(ecoRepository, context) as T
            }
            modelClass.isAssignableFrom(QRScannerViewModel::class.java) -> {
                QRScannerViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(TrainStatusViewModel::class.java) -> {
                TrainStatusViewModel(ecoRepository) as T
            }
            modelClass.isAssignableFrom(ReportWastageViewModel::class.java) -> {
                ReportWastageViewModel(energyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
