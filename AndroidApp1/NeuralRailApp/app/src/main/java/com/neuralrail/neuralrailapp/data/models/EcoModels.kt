package com.neuralrail.neuralrailapp.data.models

// EcoCommute - Personal Sustainability Tracker
data class EcoCommuteData(
    val totalCO2Saved: Float = 0f,
    val totalTrips: Int = 0,
    val weeklyGoal: Float = 50f,
    val weeklyProgress: Float = 0f,
    val streakDays: Int = 0,
    val badges: List<Badge> = emptyList(),
    val recentTrips: List<EcoTrip> = emptyList()
)

data class EcoTrip(
    val id: String,
    val date: String,
    val from: String,
    val to: String,
    val co2Saved: Float,
    val distance: Float,
    val mode: TransportMode
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconType: BadgeType,
    val isUnlocked: Boolean,
    val progress: Float = 0f
)

enum class BadgeType { BRONZE, SILVER, GOLD, PLATINUM, SPECIAL }
enum class TransportMode { RAIL, METRO, BUS, WALK, CYCLE }

// GreenRail Companion - Real-Time Energy
data class RealTimeEnergyData(
    val trainId: String,
    val trainName: String,
    val currentEnergyUsage: Float,
    val regenerativeBrakingRecovery: Float,
    val renewableEnergyPercent: Float,
    val cityTotalSavings: Float,
    val timestamp: Long
)

data class CityEnergySummary(
    val cityName: String,
    val totalEnergySaved: Float,
    val totalTrainsRunning: Int,
    val renewablePoweredPercent: Float,
    val todaysBadge: String
)

// Smart Travel Planner
data class SmartRoute(
    val id: String,
    val from: String,
    val to: String,
    val carbonFootprint: Float,
    val duration: Int,
    val distance: Float,
    val isGreenRecommended: Boolean,
    val renewableStations: List<String>,
    val congestionLevel: CongestionLevel,
    val energyEfficiencyScore: Float
)

enum class CongestionLevel { LOW, MEDIUM, HIGH }

// Carbon Offset Hub
data class CarbonOffsetData(
    val totalEmissions: Float,
    val offsetContributions: Float,
    val availableProjects: List<OffsetProject>,
    val userContributions: List<UserContribution>
)

data class OffsetProject(
    val id: String,
    val name: String,
    val description: String,
    val type: ProjectType,
    val targetAmount: Float,
    val currentAmount: Float,
    val impactPerUnit: String
)

data class UserContribution(
    val projectId: String,
    val amount: Float,
    val date: String,
    val impactDescription: String
)

enum class ProjectType { SOLAR, REFORESTATION, EV_CHARGING, WIND }

// Green Challenge
data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val isCompleted: Boolean,
    val expiresAt: String,
    val type: ChallengeType,
    val category: ChallengeCategory = ChallengeCategory.DAILY,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.EASY,
    val targetProgress: Int = 1,
    val currentProgress: Int = 0,
    val expiresAtMillis: Long = System.currentTimeMillis() + 86400000 // 24 hours default
)

data class AcceptedChallenge(
    val challenge: DailyChallenge,
    val acceptedAt: Long = System.currentTimeMillis(),
    val progress: Int = 0,
    val isActive: Boolean = true
)

data class UserChallengeStats(
    val totalPoints: Int,
    val currentStreak: Int,
    val completedChallenges: Int,
    val rank: Int,
    val weeklyLeaderboard: List<LeaderboardEntry>,
    val dailyStreak: Int = 0,
    val weeklyStreak: Int = 0,
    val longestStreak: Int = 0,
    val streakHistory: List<StreakDay> = emptyList()
)

data class StreakDay(
    val date: String,
    val completed: Boolean,
    val challengesCompleted: Int = 0
)

data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val points: Int,
    val rank: Int,
    val avatarUrl: String? = null,
    val streak: Int = 0
)

enum class ChallengeType { TRAVEL, WALK, OFF_PEAK, SHARE, REPORT }
enum class ChallengeCategory { DAILY, WEEKLY, SPECIAL, COMMUNITY }
enum class ChallengeDifficulty { EASY, MEDIUM, HARD }

// Community Energy Watch
data class EnergyReport(
    val id: String,
    val type: ReportType,
    val location: String,
    val description: String,
    val status: ReportStatus,
    val timestamp: String,
    val upvotes: Int
)

enum class ReportType { LIGHTS_ON, IDLING_ENGINE, FAULTY_SOLAR, AC_WASTE, OTHER }
enum class ReportStatus { PENDING, INVESTIGATING, RESOLVED }

data class CommunityStats(
    val totalReports: Int,
    val resolvedReports: Int,
    val energySavedFromReports: Float,
    val topContributors: List<String>
)

// Rail Awareness Hub
data class EducationContent(
    val id: String,
    val title: String,
    val content: String,
    val type: ContentType,
    val imageUrl: String? = null
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

data class FactOfTheDay(
    val fact: String,
    val source: String,
    val relatedStat: String
)

enum class ContentType { ARTICLE, INFOGRAPHIC, VIDEO, QUIZ }

// Train Status & Delay Information
data class TrainStatus(
    val trainId: String,
    val trainNumber: String,
    val trainName: String,
    val currentStatus: TrainRunningStatus,
    val currentLocation: String,
    val nextStation: String,
    val expectedArrival: String,
    val delay: Int, // in minutes
    val delayReason: DelayReason?,
    val lastUpdated: String,
    val route: List<StationStop>
)

data class StationStop(
    val stationName: String,
    val stationCode: String,
    val scheduledArrival: String,
    val actualArrival: String?,
    val scheduledDeparture: String,
    val actualDeparture: String?,
    val platform: String?,
    val status: StopStatus
)

enum class TrainRunningStatus {
    ON_TIME,
    DELAYED,
    STOPPED,
    CANCELLED,
    DIVERTED,
    RESCHEDULED
}

enum class DelayReason {
    SIGNAL_FAILURE,
    TRACK_MAINTENANCE,
    WEATHER_CONDITIONS,
    TECHNICAL_ISSUE,
    PASSENGER_EMERGENCY,
    SECURITY_CHECK,
    CONGESTION,
    ACCIDENT_AHEAD,
    POWER_FAILURE,
    CREW_CHANGE,
    UNKNOWN
}

enum class StopStatus {
    COMPLETED,
    CURRENT,
    UPCOMING,
    SKIPPED
}

// Train Energy Data for QR scanning
data class TrainEnergyData(
    val currentPowerUsage: Float,
    val regenerativeRecovery: Float,
    val renewablePercent: Float,
    val co2SavedToday: Float,
    val totalKmTraveled: Float,
    val energyEfficiencyScore: Int,
    val isElectric: Boolean,
    val solarPoweredCoaches: Int
)

// QR Scanner Result
data class QRScanResult(
    val rawValue: String,
    val type: QRContentType,
    val trainInfo: TrainStatus? = null,
    val ticketInfo: TicketInfo? = null,
    val stationInfo: StationInfo? = null,
    val energyData: TrainEnergyData? = null
)

data class TicketInfo(
    val pnr: String,
    val trainNumber: String,
    val trainName: String,
    val from: String,
    val to: String,
    val journeyDate: String,
    val coach: String,
    val seat: String,
    val passengerName: String,
    val status: String
)

data class StationInfo(
    val stationCode: String,
    val stationName: String,
    val platforms: Int,
    val amenities: List<String>,
    val isGreenStation: Boolean,
    val solarCapacity: Float
)

enum class QRContentType {
    TRAIN_INFO,
    TICKET,
    STATION,
    UNKNOWN
}
