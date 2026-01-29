package com.neuralrail.neuralrailapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings repository with SharedPreferences persistence for session management
 */
object SettingsRepository {
    private const val PREFS_NAME = "neuralrail_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_CO2_SAVED = "user_co2_saved"
    private const val KEY_USER_TRIPS = "user_trips"
    private const val KEY_USER_STREAK = "user_streak"
    private const val KEY_USER_POINTS = "user_points"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_LANGUAGE_SELECTED = "language_selected"
    
    private var prefs: SharedPreferences? = null
    
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()
    
    private val _locationEnabled = MutableStateFlow(true)
    val locationEnabled: StateFlow<Boolean> = _locationEnabled.asStateFlow()
    
    private val _dataSyncEnabled = MutableStateFlow(true)
    val dataSyncEnabled: StateFlow<Boolean> = _dataSyncEnabled.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Session management
    fun saveSession(email: String, name: String, userId: Int, co2Saved: Double, trips: Int, streak: Int, points: Int) {
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putInt(KEY_USER_ID, userId)
            putFloat(KEY_USER_CO2_SAVED, co2Saved.toFloat())
            putInt(KEY_USER_TRIPS, trips)
            putInt(KEY_USER_STREAK, streak)
            putInt(KEY_USER_POINTS, points)
            apply()
        }
    }
    
    fun clearSession() {
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ID)
            remove(KEY_USER_CO2_SAVED)
            remove(KEY_USER_TRIPS)
            remove(KEY_USER_STREAK)
            remove(KEY_USER_POINTS)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean = prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
    
    fun getSavedEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)
    fun getSavedName(): String? = prefs?.getString(KEY_USER_NAME, null)
    fun getSavedUserId(): Int = prefs?.getInt(KEY_USER_ID, 0) ?: 0
    fun getSavedCo2Saved(): Double = prefs?.getFloat(KEY_USER_CO2_SAVED, 0f)?.toDouble() ?: 0.0
    fun getSavedTrips(): Int = prefs?.getInt(KEY_USER_TRIPS, 0) ?: 0
    fun getSavedStreak(): Int = prefs?.getInt(KEY_USER_STREAK, 0) ?: 0
    fun getSavedPoints(): Int = prefs?.getInt(KEY_USER_POINTS, 0) ?: 0
    
    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }
    
    fun setDarkModeEnabled(enabled: Boolean) {
        _darkModeEnabled.value = enabled
    }
    
    fun setLocationEnabled(enabled: Boolean) {
        _locationEnabled.value = enabled
    }
    
    fun setDataSyncEnabled(enabled: Boolean) {
        _dataSyncEnabled.value = enabled
    }
    
    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }
    
    // Language management
    fun setLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
        prefs?.edit()?.apply {
            putString(KEY_LANGUAGE, languageCode)
            putBoolean(KEY_LANGUAGE_SELECTED, true)
            apply()
        }
    }
    
    fun getLanguage(): String {
        return prefs?.getString(KEY_LANGUAGE, "en") ?: "en"
    }
    
    fun isLanguageSelected(): Boolean {
        return prefs?.getBoolean(KEY_LANGUAGE_SELECTED, false) ?: false
    }
    
    fun loadLanguage() {
        _selectedLanguage.value = getLanguage()
    }
}
