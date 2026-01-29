package com.neuralrail.neuralrailapp.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a user-submitted energy wastage report.
 * Renamed from EnergyReport to avoid conflict with EcoModels.
 */
@Serializable
data class WastageReport(
    val id: String = "",
    val userId: String,
    val description: String,
    val location: String,
    val timestamp: Long,
    val status: String = "PENDING", // PENDING, RESOLVED, VERIFIED
    val aiAnalysis: GeminiAnalysisResult? = null
)
