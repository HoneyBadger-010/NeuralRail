package com.neuralrail.neuralrailapp.data

data class EnergyData(
    val trainId: String,
    val energyUsage: Float,
    val regenerativeBrakingRecovery: Float
)