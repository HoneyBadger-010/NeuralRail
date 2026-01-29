package com.neuralrail.neuralrailapp.domain

import com.neuralrail.neuralrailapp.data.EnergyData
import com.neuralrail.neuralrailapp.data.RailRepository
import com.neuralrail.neuralrailapp.data.UiState
import kotlinx.coroutines.flow.Flow

class GetEnergyDataUseCase(private val railRepository: RailRepository) {
    operator fun invoke(trainId: String): Flow<UiState<EnergyData>> = railRepository.getEnergyData(trainId)
}