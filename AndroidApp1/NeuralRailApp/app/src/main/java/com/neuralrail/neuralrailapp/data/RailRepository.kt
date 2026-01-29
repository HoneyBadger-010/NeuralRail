package com.neuralrail.neuralrailapp.data

import com.neuralrail.neuralrailapp.data.EnergyData
import com.neuralrail.neuralrailapp.data.TravelRoute
import kotlinx.coroutines.flow.Flow

interface RailRepository {
    fun getTravelRoute(routeId: String): Flow<UiState<TravelRoute>>
    fun getEnergyData(trainId: String): Flow<UiState<EnergyData>>
}