package com.neuralrail.neuralrailapp.domain

import com.neuralrail.neuralrailapp.data.RailRepository
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.TravelRoute
import kotlinx.coroutines.flow.Flow

class GetTravelRouteUseCase(private val railRepository: RailRepository) {
    operator fun invoke(routeId: String): Flow<UiState<TravelRoute>> = railRepository.getTravelRoute(routeId)
}