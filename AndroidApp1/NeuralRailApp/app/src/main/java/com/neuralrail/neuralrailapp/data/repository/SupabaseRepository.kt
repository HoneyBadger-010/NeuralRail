package com.neuralrail.neuralrailapp.data.repository

import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {
    private val api = SupabaseApi()
    
    // =====================================================
    // USER OPERATIONS
    // =====================================================
    
    suspend fun getUser(userId: Int): Result<UserDto?> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getUserById(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserByEmail(email: String): Result<UserDto?> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getUserByEmail(email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUser(email: String, name: String, phone: String = "", passwordHash: String): Result<UserDto> = 
        withContext(Dispatchers.IO) {
            try {
                val user = UserDto(
                    email = email,
                    full_name = name,
                    phone_number = phone.ifBlank { null },
                    password_hash = passwordHash
                )
                Result.success(api.createUser(user))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    suspend fun updateUserStats(userId: Int, co2Saved: Double, points: Int): Result<UserDto> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = api.getUserById(userId) ?: throw Exception("User not found")
                val updatedUser = currentUser.copy(
                    total_co2_saved = currentUser.total_co2_saved + co2Saved,
                    total_points = currentUser.total_points + points,
                    total_trips = currentUser.total_trips + 1
                )
                Result.success(api.updateUser(userId, updatedUser))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    
    // =====================================================
    // TRAIN OPERATIONS
    // =====================================================
    
    suspend fun getTrains(): Result<List<TrainDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getTrains())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTrainByNumber(number: String): Result<TrainDto?> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getTrainByNumber(number))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTrainStatus(trainId: Int): Result<TrainStatusDto?> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getTrainStatus(trainId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllTrainStatuses(): Result<List<TrainStatusDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getAllTrainStatuses())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =====================================================
    // STATION OPERATIONS
    // =====================================================
    
    suspend fun getStations(): Result<List<StationDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getStations())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGreenStations(): Result<List<StationDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getGreenStations())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =====================================================
    // ECO TRIP OPERATIONS
    // =====================================================
    
    suspend fun getUserTrips(userId: Int): Result<List<EcoTripDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getUserTrips(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logEcoTrip(
        userId: Int,
        fromLocation: String,
        toLocation: String,
        transportMode: String,
        distanceKm: Double
    ): Result<EcoTripDto> = withContext(Dispatchers.IO) {
        try {
            val co2Saved = calculateCo2Saved(distanceKm, transportMode)
            val trip = EcoTripDto(
                user_id = userId,
                trip_date = java.time.LocalDate.now().toString(),
                from_location = fromLocation,
                to_location = toLocation,
                transport_mode = transportMode,
                distance_km = distanceKm,
                co2_saved_kg = co2Saved
            )
            Result.success(api.createTrip(trip))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateCo2Saved(distanceKm: Double, mode: String): Double {
        val carEmission = 0.21 // kg CO2 per km
        val modeEmission = when (mode) {
            "RAIL" -> 0.041
            "METRO" -> 0.035
            "BUS" -> 0.089
            "WALK", "CYCLE" -> 0.0
            else -> 0.041
        }
        return distanceKm * (carEmission - modeEmission)
    }

    
    // =====================================================
    // BADGES & CHALLENGES
    // =====================================================
    
    suspend fun getBadges(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getBadges())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveChallenges(): Result<List<DailyChallengeDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getActiveChallenges())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =====================================================
    // CARBON OFFSET PROJECTS
    // =====================================================
    
    suspend fun getOffsetProjects(): Result<List<OffsetProjectDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getOffsetProjects())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun contributeToProject(userId: Int, projectId: Int, amount: Double): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val co2Offset = amount * 0.1 // Example: 1 unit = 0.1 kg CO2
                api.contributeToProject(userId, projectId, amount, co2Offset)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // =====================================================
    // ENERGY REPORTS (COMMUNITY WATCH)
    // =====================================================
    
    suspend fun getEnergyReports(): Result<List<EnergyReportDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getEnergyReports())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun submitEnergyReport(
        userId: Int,
        reportType: String,
        location: String,
        description: String
    ): Result<EnergyReportDto> = withContext(Dispatchers.IO) {
        try {
            val report = EnergyReportDto(
                user_id = userId,
                report_type = reportType,
                location = location,
                description = description
            )
            Result.success(api.createEnergyReport(report))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =====================================================
    // EDUCATION HUB
    // =====================================================
    
    suspend fun getEducationContent(): Result<List<EducationContentDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getEducationContent())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuizQuestions(): Result<List<QuizQuestionDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getQuizQuestions())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =====================================================
    // CITY ENERGY & LEADERBOARD
    // =====================================================
    
    suspend fun getCityEnergySummary(): Result<List<CityEnergySummaryDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getCityEnergySummary())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLeaderboard(limit: Int = 10): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getLeaderboard(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
