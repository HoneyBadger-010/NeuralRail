package com.neuralrail.neuralrailapp.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// =====================================================
// DATA TRANSFER OBJECTS (DTOs)
// =====================================================

@Serializable
data class UserDto(
    val user_id: Int? = null,
    val email: String,
    val phone_number: String? = null,
    val password_hash: String? = null,
    val full_name: String? = null,
    val profile_image_url: String? = null,
    val total_co2_saved: Double = 0.0,
    val total_trips: Int = 0,
    val streak_days: Int = 0,
    val total_points: Int = 0,
    val is_active: Boolean = true,
    val is_verified: Boolean = false
)

@Serializable
data class StationDto(
    val station_id: Int? = null,
    val station_code: String,
    val station_name: String,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val platforms: Int = 1,
    val is_green_station: Boolean = false,
    val solar_capacity_kw: Double = 0.0
)

@Serializable
data class TrainDto(
    val train_id: Int? = null,
    val train_number: String,
    val train_name: String,
    val train_type: String,
    val source_station_id: Int? = null,
    val destination_station_id: Int? = null,
    val total_distance_km: Double? = null,
    val is_electric: Boolean = true,
    val energy_efficiency_rating: Double? = null
)


@Serializable
data class TrainStatusDto(
    val status_id: Int? = null,
    val train_id: Int,
    val current_status: String,
    val current_location: String? = null,
    val current_station_id: Int? = null,
    val next_station_id: Int? = null,
    val expected_arrival: String? = null,
    val delay_minutes: Int = 0,
    val delay_reason: String? = null,
    val current_energy_usage_kwh: Double? = null,
    val regenerative_braking_recovery: Double? = null,
    val renewable_energy_percent: Double? = null
)

@Serializable
data class EcoTripDto(
    val trip_id: Int? = null,
    val user_id: Int,
    val trip_date: String,
    val from_station_id: Int? = null,
    val to_station_id: Int? = null,
    val from_location: String? = null,
    val to_location: String? = null,
    val transport_mode: String,
    val distance_km: Double,
    val co2_saved_kg: Double,
    val duration_minutes: Int? = null,
    val train_id: Int? = null
)

@Serializable
data class BadgeDto(
    val badge_id: Int? = null,
    val badge_name: String,
    val badge_description: String? = null,
    val badge_type: String,
    val icon_url: String? = null,
    val requirement_type: String? = null,
    val requirement_value: Double? = null,
    val points_reward: Int = 0
)

@Serializable
data class DailyChallengeDto(
    val challenge_id: Int? = null,
    val title: String,
    val description: String? = null,
    val challenge_type: String,
    val points_reward: Int,
    val target_value: Double? = null,
    val is_active: Boolean = true
)

@Serializable
data class OffsetProjectDto(
    val project_id: Int? = null,
    val project_name: String,
    val project_description: String? = null,
    val project_type: String,
    val target_amount: Double,
    val current_amount: Double = 0.0,
    val impact_per_unit: String? = null,
    val location: String? = null,
    val image_url: String? = null,
    val is_active: Boolean = true
)


@Serializable
data class EnergyReportDto(
    val report_id: Int? = null,
    val user_id: Int,
    val report_type: String,
    val location: String,
    val station_id: Int? = null,
    val description: String? = null,
    val status: String = "PENDING",
    val upvotes: Int = 0,
    val image_url: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class EducationContentDto(
    val content_id: Int? = null,
    val title: String,
    val content: String,
    val content_type: String,
    val image_url: String? = null,
    val video_url: String? = null,
    val read_time_minutes: Int? = null,
    val is_featured: Boolean = false,
    val view_count: Int = 0
)

@Serializable
data class QuizQuestionDto(
    val question_id: Int? = null,
    val question_text: String,
    val options: String, // JSON string
    val correct_answer_index: Int,
    val explanation: String? = null,
    val difficulty: String = "MEDIUM",
    val points: Int = 10
)

@Serializable
data class CityEnergySummaryDto(
    val summary_id: Int? = null,
    val city_name: String,
    val summary_date: String,
    val total_energy_saved_kwh: Double = 0.0,
    val total_trains_running: Int = 0,
    val renewable_powered_percent: Double = 0.0,
    val todays_badge: String? = null,
    val co2_saved_tonnes: Double = 0.0
)

// =====================================================
// SUPABASE API SERVICE
// =====================================================

class SupabaseApi {
    private val client = SupabaseClient.httpClient
    private val baseUrl = SupabaseConfig.REST_URL
    
    // Users
    suspend fun getUsers(): List<UserDto> {
        val response = client.get("$baseUrl/users")
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("API Error: $errorBody")
        }
        return response.body()
    }
    
    suspend fun getUserById(id: Int): UserDto? {
        val response = client.get("$baseUrl/users?user_id=eq.$id")
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("API Error: $errorBody")
        }
        return response.body<List<UserDto>>().firstOrNull()
    }
    
    suspend fun getUserByEmail(email: String): UserDto? {
        val response = client.get("$baseUrl/users?email=eq.$email")
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("API Error: $errorBody")
        }
        return response.body<List<UserDto>>().firstOrNull()
    }
    
    suspend fun createUser(user: UserDto): UserDto {
        val response = client.post("$baseUrl/users") {
            setBody(user)
            header("Prefer", "return=representation")
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("API Error: $errorBody")
        }
        return response.body<List<UserDto>>().first()
    }
    
    suspend fun updateUser(id: Int, user: UserDto): UserDto {
        val response = client.patch("$baseUrl/users?user_id=eq.$id") {
            setBody(user)
            header("Prefer", "return=representation")
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("API Error: $errorBody")
        }
        return response.body<List<UserDto>>().first()
    }

    
    // Stations
    suspend fun getStations(): List<StationDto> = client.get("$baseUrl/stations").body()
    
    suspend fun getGreenStations(): List<StationDto> = 
        client.get("$baseUrl/stations?is_green_station=eq.true").body()
    
    // Trains
    suspend fun getTrains(): List<TrainDto> = client.get("$baseUrl/trains").body()
    
    suspend fun getTrainByNumber(number: String): TrainDto? =
        client.get("$baseUrl/trains?train_number=eq.$number").body<List<TrainDto>>().firstOrNull()
    
    // Train Status
    suspend fun getTrainStatus(trainId: Int): TrainStatusDto? =
        client.get("$baseUrl/train_status?train_id=eq.$trainId").body<List<TrainStatusDto>>().firstOrNull()
    
    suspend fun getAllTrainStatuses(): List<TrainStatusDto> = 
        client.get("$baseUrl/train_status").body()
    
    // Eco Trips
    suspend fun getUserTrips(userId: Int): List<EcoTripDto> =
        client.get("$baseUrl/eco_trips?user_id=eq.$userId&order=trip_date.desc").body()
    
    suspend fun createTrip(trip: EcoTripDto): EcoTripDto =
        client.post("$baseUrl/eco_trips") {
            setBody(trip)
            header("Prefer", "return=representation")
        }.body<List<EcoTripDto>>().first()
    
    // Badges
    suspend fun getBadges(): List<BadgeDto> = client.get("$baseUrl/badges").body()
    
    suspend fun getUserBadges(userId: Int): List<BadgeDto> =
        client.get("$baseUrl/user_badges?user_id=eq.$userId&is_unlocked=eq.true&select=badge_id,badges(*)").body()
    
    // Daily Challenges
    suspend fun getActiveChallenges(): List<DailyChallengeDto> =
        client.get("$baseUrl/daily_challenges?is_active=eq.true").body()
    
    // Offset Projects
    suspend fun getOffsetProjects(): List<OffsetProjectDto> =
        client.get("$baseUrl/offset_projects?is_active=eq.true").body()
    
    suspend fun contributeToProject(userId: Int, projectId: Int, amount: Double, co2Offset: Double) =
        client.post("$baseUrl/user_contributions") {
            setBody(mapOf(
                "user_id" to userId,
                "project_id" to projectId,
                "amount" to amount,
                "co2_offset_kg" to co2Offset
            ))
        }
    
    // Energy Reports
    suspend fun getEnergyReports(): List<EnergyReportDto> =
        client.get("$baseUrl/energy_reports?order=created_at.desc").body()
    
    suspend fun createEnergyReport(report: EnergyReportDto): EnergyReportDto =
        client.post("$baseUrl/energy_reports") {
            setBody(report)
            header("Prefer", "return=representation")
        }.body<List<EnergyReportDto>>().first()
    
    suspend fun upvoteReport(reportId: Int, userId: Int) =
        client.post("$baseUrl/report_upvotes") {
            setBody(mapOf("report_id" to reportId, "user_id" to userId))
        }

    
    // Education Content
    suspend fun getEducationContent(): List<EducationContentDto> =
        client.get("$baseUrl/education_content").body()
    
    suspend fun getFeaturedContent(): List<EducationContentDto> =
        client.get("$baseUrl/education_content?is_featured=eq.true").body()
    
    // Quiz Questions
    suspend fun getQuizQuestions(): List<QuizQuestionDto> =
        client.get("$baseUrl/quiz_questions").body()
    
    // City Energy Summary
    suspend fun getCityEnergySummary(): List<CityEnergySummaryDto> =
        client.get("$baseUrl/city_energy_summary?order=summary_date.desc").body()
    
    suspend fun getCityEnergySummaryByCity(city: String): CityEnergySummaryDto? =
        client.get("$baseUrl/city_energy_summary?city_name=eq.$city&order=summary_date.desc&limit=1")
            .body<List<CityEnergySummaryDto>>().firstOrNull()
    
    // Leaderboard
    suspend fun getLeaderboard(limit: Int = 10): List<UserDto> =
        client.get("$baseUrl/users?order=total_points.desc&limit=$limit&is_active=eq.true").body()
}
