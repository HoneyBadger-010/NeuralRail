package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.TravelRoute
import com.neuralrail.neuralrailapp.domain.GetTravelRouteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(private val getTravelRouteUseCase: GetTravelRouteUseCase) : ViewModel() {

    private val _travelRouteState = MutableStateFlow<UiState<TravelRoute>>(UiState.Loading)
    val travelRouteState: StateFlow<UiState<TravelRoute>> = _travelRouteState.asStateFlow()

    init {
        getTravelRoute("default_route")
    }

    private fun getTravelRoute(routeId: String) {
        getTravelRouteUseCase(routeId)
            .onEach { _travelRouteState.value = it }
            .launchIn(viewModelScope)
    }
}