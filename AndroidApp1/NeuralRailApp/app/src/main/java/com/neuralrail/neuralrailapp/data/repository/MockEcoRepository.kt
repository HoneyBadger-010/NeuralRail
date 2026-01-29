package com.neuralrail.neuralrailapp.data.repository

import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockEcoRepository : EcoRepository {

    override fun getEcoCommuteData(): Flow<UiState<EcoCommuteData>> = flow {
        emit(UiState.Loading)
        delay(500)
        emit(UiState.Success(EcoCommuteData(
            totalCO2Saved = 245.5f,
            totalTrips = 48,
            weeklyGoal = 50f,
            weeklyProgress = 38.5f,
            streakDays = 12,
            badges = listOf(
                Badge("1", "Green Starter", "Complete 10 eco trips", BadgeType.BRONZE, true),
                Badge("2", "Carbon Crusher", "Save 100kg CO2", BadgeType.SILVER, true),
                Badge("3", "Eco Warrior", "Save 500kg CO2", BadgeType.GOLD, false, 0.49f),
                Badge("4", "Planet Protector", "Save 1000kg CO2", BadgeType.PLATINUM, false, 0.24f)
            ),
            recentTrips = listOf(
                EcoTrip("1", "Today", "Central Station", "Tech Park", 2.3f, 15f, TransportMode.RAIL),
                EcoTrip("2", "Yesterday", "Home", "City Center", 1.8f, 12f, TransportMode.METRO),
                EcoTrip("3", "2 days ago", "Office", "Mall", 0.5f, 3f, TransportMode.WALK)
            )
        )))
    }

    override fun logTrip(trip: EcoTrip): Flow<UiState<EcoCommuteData>> = getEcoCommuteData()

    override fun getRealTimeEnergy(trainId: String): Flow<UiState<RealTimeEnergyData>> = flow {
        emit(UiState.Loading)
        delay(300)
        emit(UiState.Success(RealTimeEnergyData(
            trainId = trainId,
            trainName = "Express 12045",
            currentEnergyUsage = 2450f,
            regenerativeBrakingRecovery = 0.32f,
            renewableEnergyPercent = 0.68f,
            cityTotalSavings = 12500f,
            timestamp = System.currentTimeMillis()
        )))
    }

    override fun getCityEnergySummary(): Flow<UiState<CityEnergySummary>> = flow {
        emit(UiState.Loading)
        delay(400)
        emit(UiState.Success(CityEnergySummary(
            cityName = "Ongole",
            totalEnergySaved = 45000f,
            totalTrainsRunning = 156,
            renewablePoweredPercent = 0.72f,
            todaysBadge = "🌟 Super Green Day!"
        )))
    }


    override fun getSmartRoutes(from: String, to: String): Flow<UiState<List<SmartRoute>>> = flow {
        emit(UiState.Loading)
        delay(600)
        emit(UiState.Success(listOf(
            SmartRoute("1", from, to, 1.2f, 35, 22f, true, listOf("Green Station", "Solar Hub"), CongestionLevel.LOW, 0.92f),
            SmartRoute("2", from, to, 2.1f, 28, 20f, false, listOf("Central"), CongestionLevel.MEDIUM, 0.78f),
            SmartRoute("3", from, to, 3.5f, 25, 18f, false, emptyList(), CongestionLevel.HIGH, 0.65f)
        )))
    }

    override fun getCarbonOffsetData(): Flow<UiState<CarbonOffsetData>> = flow {
        emit(UiState.Loading)
        delay(500)
        emit(UiState.Success(CarbonOffsetData(
            totalEmissions = 125.5f,
            offsetContributions = 85.0f,
            availableProjects = listOf(
                OffsetProject("1", "Solar Village Initiative", "Powering rural homes with solar energy", ProjectType.SOLAR, 10000f, 6500f, "1 unit = 10kg CO2 offset"),
                OffsetProject("2", "Green Forest Project", "Planting trees in urban areas", ProjectType.REFORESTATION, 5000f, 3200f, "1 unit = 5kg CO2 offset"),
                OffsetProject("3", "Community EV Charging", "Installing EV chargers in communities", ProjectType.EV_CHARGING, 8000f, 4100f, "1 unit = 8kg CO2 offset")
            ),
            userContributions = listOf(
                UserContribution("1", 50f, "Dec 5, 2024", "Offset 500kg CO2"),
                UserContribution("2", 35f, "Nov 28, 2024", "Planted 7 trees")
            )
        )))
    }

    override fun contributeToProject(projectId: String, amount: Float): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        delay(800)
        emit(UiState.Success(true))
    }

    override fun getDailyChallenges(): Flow<UiState<List<DailyChallenge>>> = flow {
        emit(UiState.Loading)
        delay(400)
        val now = System.currentTimeMillis()
        emit(UiState.Success(listOf(
            // Daily challenges
            DailyChallenge("1", "Rail Rider", "Take the train instead of cab today", 50, false, "23:59", ChallengeType.TRAVEL, ChallengeCategory.DAILY, ChallengeDifficulty.EASY, 1, 0, now + 3600000),
            DailyChallenge("2", "Walk the Mile", "Walk 1 km instead of using auto", 30, true, "23:59", ChallengeType.WALK, ChallengeCategory.DAILY, ChallengeDifficulty.EASY, 1, 1, now + 7200000),
            DailyChallenge("3", "Off-Peak Hero", "Travel during off-peak hours (10AM-4PM)", 40, false, "23:59", ChallengeType.OFF_PEAK, ChallengeCategory.DAILY, ChallengeDifficulty.MEDIUM, 1, 0, now + 14400000),
            DailyChallenge("4", "Share & Care", "Share your eco stats with 3 friends", 20, false, "23:59", ChallengeType.SHARE, ChallengeCategory.DAILY, ChallengeDifficulty.EASY, 3, 0, now + 21600000),
            // Weekly challenges
            DailyChallenge("5", "Weekly Commuter", "Take 5 train trips this week", 150, false, "7 days", ChallengeType.TRAVEL, ChallengeCategory.WEEKLY, ChallengeDifficulty.MEDIUM, 5, 2, now + 604800000),
            DailyChallenge("6", "Marathon Walker", "Walk 10 km total this week", 100, false, "7 days", ChallengeType.WALK, ChallengeCategory.WEEKLY, ChallengeDifficulty.HARD, 10, 3, now + 604800000),
            // Special challenges
            DailyChallenge("7", "Green Reporter", "Report 2 energy waste issues", 80, false, "48 hours", ChallengeType.REPORT, ChallengeCategory.SPECIAL, ChallengeDifficulty.MEDIUM, 2, 0, now + 172800000),
            // Community challenges
            DailyChallenge("8", "Community Hero", "Help the community save 1000kg CO2", 200, false, "3 days", ChallengeType.TRAVEL, ChallengeCategory.COMMUNITY, ChallengeDifficulty.HARD, 1000, 650, now + 259200000)
        )))
    }

    override fun getUserChallengeStats(): Flow<UiState<UserChallengeStats>> = flow {
        emit(UiState.Loading)
        delay(300)
        emit(UiState.Success(UserChallengeStats(
            totalPoints = 1250,
            currentStreak = 7,
            completedChallenges = 45,
            rank = 23,
            weeklyLeaderboard = listOf(
                LeaderboardEntry("1", "EcoChamp", 2500, 1, null, 15),
                LeaderboardEntry("2", "GreenRider", 2350, 2, null, 12),
                LeaderboardEntry("3", "NatureLover", 1800, 3, null, 9),
                LeaderboardEntry("4", "You", 1250, 23, null, 7),
                LeaderboardEntry("5", "GreenNewbie", 950, 45, null, 3)
            ),
            dailyStreak = 7,
            weeklyStreak = 3,
            longestStreak = 14,
            streakHistory = listOf(
                StreakDay("Mon", true, 3),
                StreakDay("Tue", true, 2),
                StreakDay("Wed", true, 4),
                StreakDay("Thu", true, 2),
                StreakDay("Fri", true, 3),
                StreakDay("Sat", true, 1),
                StreakDay("Sun", false, 0)
            )
        )))
    }

    override fun completeChallenge(challengeId: String): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        delay(500)
        emit(UiState.Success(true))
    }

    override fun submitReport(report: EnergyReport): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        delay(600)
        emit(UiState.Success(true))
    }

    override fun getReports(): Flow<UiState<List<EnergyReport>>> = flow {
        emit(UiState.Loading)
        delay(400)
        emit(UiState.Success(listOf(
            EnergyReport("1", ReportType.LIGHTS_ON, "Platform 3, Central Station", "Lights on at empty platform after 11 PM", ReportStatus.INVESTIGATING, "2 hours ago", 12),
            EnergyReport("2", ReportType.IDLING_ENGINE, "Yard B", "Train idling for 30+ minutes", ReportStatus.RESOLVED, "Yesterday", 25),
            EnergyReport("3", ReportType.FAULTY_SOLAR, "Green Station Roof", "Solar panel not functioning", ReportStatus.PENDING, "3 days ago", 8)
        )))
    }

    override fun getCommunityStats(): Flow<UiState<CommunityStats>> = flow {
        emit(UiState.Loading)
        delay(300)
        emit(UiState.Success(CommunityStats(
            totalReports = 1250,
            resolvedReports = 980,
            energySavedFromReports = 15000f,
            topContributors = listOf("EcoWatcher", "GreenEye", "PowerSaver")
        )))
    }

    override fun getEducationContent(): Flow<UiState<List<EducationContent>>> = flow {
        emit(UiState.Loading)
        delay(400)
        emit(UiState.Success(listOf(
            EducationContent(
                "1",
                "How Regenerative Braking Works",
                """When a train brakes, the electric motors run in reverse, acting as generators that convert the train's kinetic energy back into electricity.

## The Science Behind It

Regenerative braking is one of the most innovative technologies in modern rail transport. Instead of wasting energy as heat through traditional friction brakes, electric trains can recover up to 30% of the energy used during acceleration.

• The electric motor switches to generator mode during braking
• Kinetic energy is converted to electrical energy
• This electricity is fed back into the power grid or stored in batteries
• The process significantly reduces overall energy consumption

💡 A single train journey can recover enough energy to power 50 homes for a day!

## Environmental Impact

This technology has revolutionized sustainable transportation. Indian Railways alone saves approximately 2.5 billion kWh annually through regenerative braking systems.

The recovered energy reduces:
• Carbon emissions by up to 25%
• Dependency on fossil fuels
• Overall operational costs

## Future Developments

Next-generation trains are being designed with even more efficient regenerative systems, potentially recovering up to 45% of braking energy.""",
                ContentType.ARTICLE
            ),
            EducationContent(
                "2",
                "India's Green Railway Journey",
                """Indian Railways has committed to becoming net-zero by 2030, making it one of the most ambitious sustainability goals in the transportation sector.

## The Net-Zero Mission

With over 68,000 km of track and 13,000 trains running daily, Indian Railways is the world's fourth-largest rail network. The transformation to green energy is massive in scale.

• 90% of routes are now electrified
• 960+ stations powered by solar energy
• LED lighting installed across 7,000+ stations
• Bio-toilets fitted in all passenger coaches

💡 Indian Railways transports 23 million passengers daily - that's more than Australia's entire population!

## Key Achievements

The railway has made remarkable progress:
• Reduced carbon footprint by 33% since 2015
• Installed 1 GW of solar capacity
• Planted 50 million trees along rail corridors
• Converted 100% of diesel locos to electric on main routes

## What's Next

By 2030, Indian Railways aims to:
• Achieve complete carbon neutrality
• Run 100% on renewable energy
• Become the world's first green railway network""",
                ContentType.INFOGRAPHIC
            ),
            EducationContent(
                "3",
                "Solar-Powered Stations",
                """Over 960 stations now run on solar power, reducing grid dependency and setting new standards for sustainable infrastructure.

## The Solar Revolution

Railway stations are ideal candidates for solar power - they have large roof areas, consistent energy needs, and operate during peak sunlight hours.

• Average station generates 50-500 kW of solar power
• Excess energy is fed back to the grid
• Reduces electricity bills by up to 70%
• Provides backup power during outages

💡 The solar panels at New Delhi station alone save 2,000 tonnes of CO2 annually!

## How It Works

Solar installations at stations include:
• Rooftop photovoltaic panels
• Solar trees in parking areas
• Building-integrated photovoltaics
• Solar-powered signage and lighting

## Success Stories

Some remarkable achievements:
• Guwahati station: 100% solar powered
• Varanasi station: 1 MW solar plant
• Secunderabad: Solar-powered platform shelters
• Mumbai suburban: Solar-powered ticket machines

## Benefits Beyond Energy

Solar stations also provide:
• Shade for passengers
• Reduced urban heat island effect
• Educational value for visitors
• Job creation in green technology""",
                ContentType.ARTICLE
            )
        )))
    }

    override fun getQuizQuestions(): Flow<UiState<List<QuizQuestion>>> = flow {
        emit(UiState.Loading)
        delay(300)
        
        val allQuestions = listOf(
            QuizQuestion("1", "What percentage of Indian Railways is electrified?", listOf("50%", "90%", "70%", "95%"), 1, "As of 2024, over 90% of Indian Railways is electrified!"),
            QuizQuestion("2", "How much CO2 does rail save vs road per km?", listOf("80%", "50%", "70%", "90%"), 0, "Rail transport produces about 80% less CO2 than road transport."),
            QuizQuestion("3", "What is regenerative braking?", listOf("Using solar panels", "Using wind power", "Manual braking", "Converting kinetic energy to electricity"), 3, "Regenerative braking converts the train's kinetic energy back into electricity when slowing down."),
            QuizQuestion("4", "How many stations in India are solar-powered?", listOf("100+", "960+", "500+", "1500+"), 1, "Over 960 railway stations in India now run on solar power!"),
            QuizQuestion("5", "What is Indian Railways' net-zero target year?", listOf("2030", "2025", "2040", "2050"), 0, "Indian Railways aims to become net-zero carbon emitter by 2030."),
            QuizQuestion("6", "Which is the greenest mode of mass transport?", listOf("Bus", "Airplane", "Metro", "Electric Train"), 3, "Electric trains are the most energy-efficient mass transport mode."),
            QuizQuestion("7", "What percentage of energy can regenerative braking recover?", listOf("10-15%", "40-50%", "20-30%", "60-70%"), 2, "Regenerative braking can recover 20-30% of the energy used during acceleration."),
            QuizQuestion("8", "How much CO2 does a single tree absorb per year?", listOf("21 kg", "5 kg", "10 kg", "50 kg"), 0, "A mature tree absorbs approximately 21 kg of CO2 per year."),
            QuizQuestion("9", "What is the world's largest railway network by employees?", listOf("China Railways", "US Railways", "Russian Railways", "Indian Railways"), 3, "Indian Railways is the world's largest employer with over 1.3 million employees."),
            QuizQuestion("10", "Which fuel type produces zero direct emissions?", listOf("Diesel", "Electric", "CNG", "Petrol"), 1, "Electric trains produce zero direct emissions at the point of use."),
            QuizQuestion("11", "What is carbon footprint?", listOf("Total greenhouse gas emissions", "Shoe size", "Walking distance", "Train length"), 0, "Carbon footprint is the total amount of greenhouse gases produced by our activities."),
            QuizQuestion("12", "How much energy does LED lighting save vs traditional?", listOf("30%", "50%", "90%", "75%"), 3, "LED lighting uses about 75% less energy than traditional incandescent bulbs."),
            QuizQuestion("13", "What is the main greenhouse gas from transport?", listOf("Carbon Dioxide", "Oxygen", "Nitrogen", "Hydrogen"), 0, "Carbon dioxide (CO2) is the primary greenhouse gas emitted from transportation."),
            QuizQuestion("14", "Which country has the fastest trains?", listOf("India", "China", "Japan", "France"), 1, "China has the fastest commercial trains, with speeds up to 350 km/h."),
            QuizQuestion("15", "What does 'sustainable transport' mean?", listOf("Fast transport", "Eco-friendly transport", "Cheap transport", "Air transport"), 1, "Sustainable transport minimizes environmental impact while meeting mobility needs.")
        )
        
        // Randomly select 5 questions using day-based seed for consistency within a day
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        val shuffled = allQuestions.shuffled(kotlin.random.Random(dayOfYear))
        val dailyQuestions = shuffled.take(5)
        
        emit(UiState.Success(dailyQuestions))
    }

    override fun getFactOfTheDay(): Flow<UiState<FactOfTheDay>> = flow {
        emit(UiState.Loading)
        delay(200)
        emit(UiState.Success(FactOfTheDay(
            fact = "Indian Railways saved 12,000 tonnes of CO2 today through regenerative braking!",
            source = "Railway Energy Dashboard",
            relatedStat = "That's equivalent to planting 550,000 trees!"
        )))
    }

    override fun getTrainStatus(trainNumber: String): Flow<UiState<TrainStatus>> = flow {
        emit(UiState.Loading)
        delay(500)
        emit(UiState.Success(TrainStatus(
            trainId = "T12045",
            trainNumber = trainNumber.ifEmpty { "12045" },
            trainName = "Mumbai Rajdhani Express",
            currentStatus = TrainRunningStatus.DELAYED,
            currentLocation = "Between Vadodara and Surat",
            nextStation = "Surat Junction",
            expectedArrival = "14:35",
            delay = 25,
            delayReason = DelayReason.SIGNAL_FAILURE,
            lastUpdated = "2 min ago",
            route = listOf(
                StationStop("Mumbai Central", "MMCT", "06:00", "06:00", "06:05", "06:05", "1", StopStatus.COMPLETED),
                StationStop("Borivali", "BVI", "06:25", "06:25", "06:27", "06:27", "3", StopStatus.COMPLETED),
                StationStop("Vadodara Junction", "BRC", "10:30", "10:45", "10:35", "10:50", "2", StopStatus.COMPLETED),
                StationStop("Surat Junction", "ST", "12:10", null, "12:15", null, "4", StopStatus.CURRENT),
                StationStop("Ratlam Junction", "RTM", "15:20", null, "15:25", null, null, StopStatus.UPCOMING),
                StationStop("New Delhi", "NDLS", "22:30", null, "22:30", null, null, StopStatus.UPCOMING)
            )
        )))
    }

    override fun getLiveTrains(): Flow<UiState<List<TrainStatus>>> = flow {
        emit(UiState.Loading)
        delay(400)
        emit(UiState.Success(listOf(
            TrainStatus("T12045", "12045", "Mumbai Rajdhani", TrainRunningStatus.DELAYED, "Near Surat", "Surat Jn", "14:35", 25, DelayReason.SIGNAL_FAILURE, "2 min ago", emptyList()),
            TrainStatus("T12951", "12951", "Mumbai Rajdhani", TrainRunningStatus.ON_TIME, "Kota Junction", "Sawai Madhopur", "16:20", 0, null, "1 min ago", emptyList()),
            TrainStatus("T12301", "12301", "Howrah Rajdhani", TrainRunningStatus.STOPPED, "Allahabad Jn", "Allahabad Jn", "18:45", 45, DelayReason.PASSENGER_EMERGENCY, "Just now", emptyList()),
            TrainStatus("T12627", "12627", "Karnataka Express", TrainRunningStatus.DELAYED, "Near Pune", "Pune Jn", "11:30", 15, DelayReason.CONGESTION, "3 min ago", emptyList()),
            TrainStatus("T12839", "12839", "Chennai Mail", TrainRunningStatus.ON_TIME, "Vijayawada", "Nellore", "13:00", 0, null, "5 min ago", emptyList())
        )))
    }

    override fun parseQRCode(rawValue: String): Flow<UiState<QRScanResult>> = flow {
        emit(UiState.Loading)
        delay(300)
        // Simulate parsing different QR types
        val result = when {
            // NeuralRail Train 1 - Vande Bharat Express (On Time)
            rawValue.contains("NR12045") -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.TRAIN_INFO,
                trainInfo = TrainStatus(
                    trainId = "T12045",
                    trainNumber = "12045",
                    trainName = "Vande Bharat Express",
                    currentStatus = TrainRunningStatus.ON_TIME,
                    currentLocation = "Approaching Vijayawada Junction",
                    nextStation = "Vijayawada Junction",
                    expectedArrival = "10:45",
                    delay = 0,
                    delayReason = null,
                    lastUpdated = "Just now",
                    route = listOf(
                        StationStop("Chennai Central", "MAS", "06:00", "06:00", "06:05", "06:05", "1", StopStatus.COMPLETED),
                        StationStop("Nellore", "NLR", "08:15", "08:15", "08:17", "08:17", "2", StopStatus.COMPLETED),
                        StationStop("Ongole", "OGL", "09:30", "09:30", "09:32", "09:32", "1", StopStatus.COMPLETED),
                        StationStop("Vijayawada Junction", "BZA", "10:45", null, "10:50", null, "3", StopStatus.CURRENT),
                        StationStop("Warangal", "WL", "12:30", null, "12:32", null, null, StopStatus.UPCOMING),
                        StationStop("Secunderabad", "SC", "14:00", null, "14:00", null, null, StopStatus.UPCOMING)
                    )
                ),
                energyData = TrainEnergyData(
                    currentPowerUsage = 2450f,
                    regenerativeRecovery = 32f,
                    renewablePercent = 78f,
                    co2SavedToday = 125.5f,
                    totalKmTraveled = 385f,
                    energyEfficiencyScore = 92,
                    isElectric = true,
                    solarPoweredCoaches = 4
                )
            )
            // NeuralRail Train 2 - Rajdhani Express (Delayed)
            rawValue.contains("NR12301") -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.TRAIN_INFO,
                trainInfo = TrainStatus(
                    trainId = "T12301",
                    trainNumber = "12301",
                    trainName = "Howrah Rajdhani Express",
                    currentStatus = TrainRunningStatus.DELAYED,
                    currentLocation = "Near Kanpur Central",
                    nextStation = "Kanpur Central",
                    expectedArrival = "15:20",
                    delay = 35,
                    delayReason = DelayReason.TRACK_MAINTENANCE,
                    lastUpdated = "2 min ago",
                    route = listOf(
                        StationStop("Howrah Junction", "HWH", "08:00", "08:00", "08:10", "08:10", "9", StopStatus.COMPLETED),
                        StationStop("Asansol Junction", "ASN", "10:15", "10:20", "10:18", "10:25", "3", StopStatus.COMPLETED),
                        StationStop("Dhanbad Junction", "DHN", "11:30", "11:45", "11:35", "11:50", "1", StopStatus.COMPLETED),
                        StationStop("Gaya Junction", "GAYA", "13:00", "13:25", "13:05", "13:30", "2", StopStatus.COMPLETED),
                        StationStop("Kanpur Central", "CNB", "15:20", null, "15:25", null, "5", StopStatus.CURRENT),
                        StationStop("New Delhi", "NDLS", "18:30", null, "18:35", null, null, StopStatus.UPCOMING)
                    )
                ),
                energyData = TrainEnergyData(
                    currentPowerUsage = 3200f,
                    regenerativeRecovery = 28f,
                    renewablePercent = 65f,
                    co2SavedToday = 98.2f,
                    totalKmTraveled = 520f,
                    energyEfficiencyScore = 78,
                    isElectric = true,
                    solarPoweredCoaches = 2
                )
            )
            rawValue.startsWith("TRAIN:") -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.TRAIN_INFO,
                trainInfo = TrainStatus(
                    trainId = "T12045",
                    trainNumber = "12045",
                    trainName = "Mumbai Rajdhani Express",
                    currentStatus = TrainRunningStatus.DELAYED,
                    currentLocation = "Near Surat",
                    nextStation = "Surat Junction",
                    expectedArrival = "14:35",
                    delay = 25,
                    delayReason = DelayReason.SIGNAL_FAILURE,
                    lastUpdated = "Just now",
                    route = emptyList()
                )
            )
            rawValue.startsWith("PNR:") || rawValue.contains("ticket", ignoreCase = true) -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.TICKET,
                ticketInfo = TicketInfo(
                    pnr = "4521678901",
                    trainNumber = "12045",
                    trainName = "Mumbai Rajdhani Express",
                    from = "Mumbai Central",
                    to = "New Delhi",
                    journeyDate = "Dec 10, 2024",
                    coach = "A1",
                    seat = "23",
                    passengerName = "Passenger",
                    status = "Confirmed"
                )
            )
            rawValue.startsWith("STN:") -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.STATION,
                stationInfo = StationInfo(
                    stationCode = "MMCT",
                    stationName = "Mumbai Central",
                    platforms = 6,
                    amenities = listOf("WiFi", "Food Court", "Waiting Room", "ATM", "EV Charging"),
                    isGreenStation = true,
                    solarCapacity = 250f
                )
            )
            else -> QRScanResult(
                rawValue = rawValue,
                type = QRContentType.UNKNOWN
            )
        }
        emit(UiState.Success(result))
    }
}
