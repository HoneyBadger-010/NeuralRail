package com.neuralrail.neuralrailapp.presentation.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class GreenChallengeViewModel(private val repository: EcoRepository, private val context: Context) : ViewModel() {
    
    private val _challengesState = MutableStateFlow<UiState<List<DailyChallenge>>>(UiState.Loading)
    val challengesState: StateFlow<UiState<List<DailyChallenge>>> = _challengesState.asStateFlow()
    
    private val _statsState = MutableStateFlow<UiState<UserChallengeStats>>(UiState.Loading)
    val statsState: StateFlow<UiState<UserChallengeStats>> = _statsState.asStateFlow()
    
    // Accepted challenges tracking
    private val _acceptedChallenges = MutableStateFlow<Map<String, AcceptedChallenge>>(emptyMap())
    val acceptedChallenges: StateFlow<Map<String, AcceptedChallenge>> = _acceptedChallenges.asStateFlow()
    
    // Filter state
    private val _selectedCategory = MutableStateFlow<ChallengeCategory?>(null)
    val selectedCategory: StateFlow<ChallengeCategory?> = _selectedCategory.asStateFlow()
    
    private val _selectedType = MutableStateFlow<ChallengeType?>(null)
    val selectedType: StateFlow<ChallengeType?> = _selectedType.asStateFlow()
    
    // Countdown timers for expiring challenges
    private val _countdowns = MutableStateFlow<Map<String, Long>>(emptyMap())
    val countdowns: StateFlow<Map<String, Long>> = _countdowns.asStateFlow()
    
    init {
        loadData()
        startCountdownTimer()
    }
    
    private fun getLocalizedContext(): Context {
        val currentLanguage = SettingsRepository.selectedLanguage.value
        val locale = Locale(currentLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getDailyChallenges().collect { state ->
                // Replace with localized challenges
                if (state is UiState.Success) {
                    val localizedChallenges = createLocalizedChallenges()
                    _challengesState.value = UiState.Success(localizedChallenges)
                } else {
                    _challengesState.value = state
                }
            }
        }
        viewModelScope.launch {
            repository.getUserChallengeStats().collect { _statsState.value = it }
        }
    }
    
    private fun createLocalizedChallenges(): List<DailyChallenge> {
        val localizedContext = getLocalizedContext()
        val now = System.currentTimeMillis()
        
        return listOf(
            // Daily challenges
            DailyChallenge(
                "1",
                localizedContext.getString(R.string.challenge_rail_rider),
                localizedContext.getString(R.string.challenge_rail_rider_desc),
                50, false, "23:59",
                ChallengeType.TRAVEL, ChallengeCategory.DAILY, ChallengeDifficulty.EASY,
                1, 0, now + 3600000
            ),
            DailyChallenge(
                "2",
                localizedContext.getString(R.string.challenge_walk_mile),
                localizedContext.getString(R.string.challenge_walk_mile_desc),
                30, true, "23:59",
                ChallengeType.WALK, ChallengeCategory.DAILY, ChallengeDifficulty.EASY,
                1, 1, now + 7200000
            ),
            DailyChallenge(
                "3",
                localizedContext.getString(R.string.challenge_off_peak),
                localizedContext.getString(R.string.challenge_off_peak_desc),
                40, false, "23:59",
                ChallengeType.OFF_PEAK, ChallengeCategory.DAILY, ChallengeDifficulty.MEDIUM,
                1, 0, now + 14400000
            ),
            DailyChallenge(
                "4",
                localizedContext.getString(R.string.challenge_share_care),
                localizedContext.getString(R.string.challenge_share_care_desc),
                20, false, "23:59",
                ChallengeType.SHARE, ChallengeCategory.DAILY, ChallengeDifficulty.EASY,
                3, 0, now + 21600000
            ),
            // Weekly challenges
            DailyChallenge(
                "5",
                localizedContext.getString(R.string.challenge_weekly_commuter),
                localizedContext.getString(R.string.challenge_weekly_commuter_desc),
                150, false, localizedContext.getString(R.string.time_7_days),
                ChallengeType.TRAVEL, ChallengeCategory.WEEKLY, ChallengeDifficulty.MEDIUM,
                5, 2, now + 604800000
            ),
            DailyChallenge(
                "6",
                localizedContext.getString(R.string.challenge_marathon_walker),
                localizedContext.getString(R.string.challenge_marathon_walker_desc),
                100, false, localizedContext.getString(R.string.time_7_days),
                ChallengeType.WALK, ChallengeCategory.WEEKLY, ChallengeDifficulty.HARD,
                10, 3, now + 604800000
            ),
            // Special challenges
            DailyChallenge(
                "7",
                localizedContext.getString(R.string.challenge_green_reporter),
                localizedContext.getString(R.string.challenge_green_reporter_desc),
                80, false, localizedContext.getString(R.string.time_48_hours),
                ChallengeType.REPORT, ChallengeCategory.SPECIAL, ChallengeDifficulty.MEDIUM,
                2, 0, now + 172800000
            ),
            // Community challenges
            DailyChallenge(
                "8",
                localizedContext.getString(R.string.challenge_community_hero),
                localizedContext.getString(R.string.challenge_community_hero_desc),
                200, false, localizedContext.getString(R.string.time_3_days),
                ChallengeType.TRAVEL, ChallengeCategory.COMMUNITY, ChallengeDifficulty.HARD,
                1000, 650, now + 259200000
            )
        )
    }
    
    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                updateCountdowns()
            }
        }
    }
    
    private fun updateCountdowns() {
        val currentTime = System.currentTimeMillis()
        val challenges = (_challengesState.value as? UiState.Success)?.data ?: return
        
        val newCountdowns = challenges.associate { challenge ->
            val remaining = challenge.expiresAtMillis - currentTime
            challenge.id to maxOf(0, remaining)
        }
        _countdowns.value = newCountdowns
    }

    
    fun acceptChallenge(challenge: DailyChallenge) {
        val accepted = AcceptedChallenge(
            challenge = challenge,
            acceptedAt = System.currentTimeMillis(),
            progress = 0,
            isActive = true
        )
        _acceptedChallenges.value = _acceptedChallenges.value + (challenge.id to accepted)
    }
    
    fun abandonChallenge(challengeId: String) {
        _acceptedChallenges.value = _acceptedChallenges.value - challengeId
    }
    
    fun updateProgress(challengeId: String, progress: Int) {
        val current = _acceptedChallenges.value[challengeId] ?: return
        val updated = current.copy(progress = progress)
        _acceptedChallenges.value = _acceptedChallenges.value + (challengeId to updated)
        
        // Check if completed
        if (progress >= current.challenge.targetProgress) {
            completeChallenge(challengeId)
        }
    }
    
    fun completeChallenge(challengeId: String) {
        viewModelScope.launch {
            repository.completeChallenge(challengeId).collect { 
                // Mark as completed and refresh
                val current = _acceptedChallenges.value[challengeId]
                if (current != null) {
                    val completed = current.copy(isActive = false)
                    _acceptedChallenges.value = _acceptedChallenges.value + (challengeId to completed)
                }
                loadData()
            }
        }
    }
    
    fun setFilter(category: ChallengeCategory?) {
        _selectedCategory.value = category
    }
    
    fun setTypeFilter(type: ChallengeType?) {
        _selectedType.value = type
    }
    
    fun getFilteredChallenges(): List<DailyChallenge> {
        val challenges = (_challengesState.value as? UiState.Success)?.data ?: return emptyList()
        return challenges.filter { challenge ->
            val categoryMatch = _selectedCategory.value == null || challenge.category == _selectedCategory.value
            val typeMatch = _selectedType.value == null || challenge.type == _selectedType.value
            categoryMatch && typeMatch
        }
    }
    
    fun isAccepted(challengeId: String): Boolean {
        return _acceptedChallenges.value.containsKey(challengeId)
    }
    
    fun getAcceptedChallenge(challengeId: String): AcceptedChallenge? {
        return _acceptedChallenges.value[challengeId]
    }
    
    fun getTimeRemaining(challengeId: String): Long {
        return _countdowns.value[challengeId] ?: 0
    }
    
    fun formatTimeRemaining(millis: Long): String {
        if (millis <= 0) return getLocalizedContext().getString(R.string.time_expired)
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
    
    fun getProgressPercent(challengeId: String): Float {
        val accepted = _acceptedChallenges.value[challengeId] ?: return 0f
        return (accepted.progress.toFloat() / accepted.challenge.targetProgress).coerceIn(0f, 1f)
    }
    
    fun shareProgress(challenge: DailyChallenge): String {
        val accepted = _acceptedChallenges.value[challenge.id]
        val progress = accepted?.progress ?: 0
        return "🌿 I'm working on \"${challenge.title}\" challenge on NeuralRail!\n" +
               "Progress: $progress/${challenge.targetProgress}\n" +
               "Points: +${challenge.points} 🏆\n" +
               "#GreenChallenge #NeuralRail #EcoCommute"
    }
    
    fun getActiveAcceptedChallenges(): List<AcceptedChallenge> {
        return _acceptedChallenges.value.values.filter { it.isActive }
    }
    
    fun getCompletedAcceptedChallenges(): List<AcceptedChallenge> {
        return _acceptedChallenges.value.values.filter { !it.isActive }
    }
}
