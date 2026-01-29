package com.neuralrail.neuralrailapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockRailRepository : RailRepository {

    override fun getTravelRoute(routeId: String): Flow<UiState<TravelRoute>> = flow {
        emit(UiState.Loading)
        delay(1000)
        emit(
            UiState.Success(
                TravelRoute(
                    id = routeId,
                    name = "City Center to Green Valley",
                    co2Saved = 75.5f,
                    progress = 0.75f
                )
            )
        )
    }

    override fun getEnergyData(trainId: String): Flow<UiState<EnergyData>> = flow {
        emit(UiState.Loading)
        delay(1500)
        emit(
            UiState.Success(
                EnergyData(
                    trainId = trainId,
                    energyUsage = 120.5f,
                    regenerativeBrakingRecovery = 0.3f
                )
            )
        )
    }
}