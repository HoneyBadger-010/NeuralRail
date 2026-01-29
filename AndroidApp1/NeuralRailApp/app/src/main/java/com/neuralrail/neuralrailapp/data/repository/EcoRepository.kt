package com.neuralrail.neuralrailapp.data.repository

import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import kotlinx.coroutines.flow.Flow

interface EcoRepository {
    // EcoCommute
    fun getEcoCommuteData(): Flow<UiState<EcoCommuteData>>
    fun logTrip(trip: EcoTrip): Flow<UiState<EcoCommuteData>>
    
    // GreenRail Companion
    fun getRealTimeEnergy(trainId: String): Flow<UiState<RealTimeEnergyData>>
    fun getCityEnergySummary(): Flow<UiState<CityEnergySummary>>
    
    // Smart Travel Planner
    fun getSmartRoutes(from: String, to: String): Flow<UiState<List<SmartRoute>>>
    
    // Carbon Offset
    fun getCarbonOffsetData(): Flow<UiState<CarbonOffsetData>>
    fun contributeToProject(projectId: String, amount: Float): Flow<UiState<Boolean>>
    
    // Green Challenge
    fun getDailyChallenges(): Flow<UiState<List<DailyChallenge>>>
    fun getUserChallengeStats(): Flow<UiState<UserChallengeStats>>
    fun completeChallenge(challengeId: String): Flow<UiState<Boolean>>
    
    // Community Watch
    fun submitReport(report: EnergyReport): Flow<UiState<Boolean>>
    fun getReports(): Flow<UiState<List<EnergyReport>>>
    fun getCommunityStats(): Flow<UiState<CommunityStats>>
    
    // Education Hub
    fun getEducationContent(): Flow<UiState<List<EducationContent>>>
    fun getQuizQuestions(): Flow<UiState<List<QuizQuestion>>>
    fun getFactOfTheDay(): Flow<UiState<FactOfTheDay>>
    
    // Train Status
    fun getTrainStatus(trainNumber: String): Flow<UiState<TrainStatus>>
    fun getLiveTrains(): Flow<UiState<List<TrainStatus>>>
    
    // QR Scanner
    fun parseQRCode(rawValue: String): Flow<UiState<QRScanResult>>
}
