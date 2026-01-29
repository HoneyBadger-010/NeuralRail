package com.neuralrail.neuralrailapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.data.remote.UserDto
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository
import com.neuralrail.neuralrailapp.data.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserDto? = null,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val repository = SupabaseRepository()
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Set to true to bypass Supabase and allow any login (for testing)
    private val bypassAuth = true
    
    init {
        // Check for saved session on init
        checkSavedSession()
    }
    
    private fun checkSavedSession() {
        if (SettingsRepository.isLoggedIn()) {
            val savedEmail = SettingsRepository.getSavedEmail()
            val savedName = SettingsRepository.getSavedName()
            if (savedEmail != null && savedName != null) {
                val savedUser = UserDto(
                    user_id = SettingsRepository.getSavedUserId(),
                    email = savedEmail,
                    full_name = savedName,
                    total_co2_saved = SettingsRepository.getSavedCo2Saved(),
                    total_trips = SettingsRepository.getSavedTrips(),
                    streak_days = SettingsRepository.getSavedStreak(),
                    total_points = SettingsRepository.getSavedPoints()
                )
                _authState.value = AuthState(
                    isLoggedIn = true,
                    currentUser = savedUser
                )
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // Bypass mode - allow any credentials
            if (bypassAuth) {
                kotlinx.coroutines.delay(500) // Simulate network delay
                val mockUser = UserDto(
                    user_id = 1,
                    email = email,
                    full_name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    total_co2_saved = 245.50,
                    total_trips = 42,
                    streak_days = 15,
                    total_points = 2850
                )
                
                // Save session to SharedPreferences
                SettingsRepository.saveSession(
                    email = mockUser.email,
                    name = mockUser.full_name ?: "User",
                    userId = mockUser.user_id ?: 0,
                    co2Saved = mockUser.total_co2_saved,
                    trips = mockUser.total_trips,
                    streak = mockUser.streak_days,
                    points = mockUser.total_points
                )
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = mockUser,
                    error = null
                )
                return@launch
            }
            
            try {
                val result = repository.getUserByEmail(email)
                result.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            val passwordHash = hashPassword(password)
                            if (user.password_hash == passwordHash) {
                                _authState.value = _authState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    currentUser = user,
                                    error = null
                                )
                            } else {
                                _authState.value = _authState.value.copy(
                                    isLoading = false,
                                    error = "Invalid password"
                                )
                            }
                        } else {
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = "User not found. Please register first."
                            )
                        }
                    },
                    onFailure = { e ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Login failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    
    fun register(email: String, fullName: String, phone: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // Bypass mode - simulate successful registration
            if (bypassAuth) {
                kotlinx.coroutines.delay(500) // Simulate network delay
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    registrationSuccess = true,
                    error = null
                )
                return@launch
            }
            
            try {
                // Check if user already exists
                val existingUser = repository.getUserByEmail(email)
                existingUser.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = "Email already registered. Please login."
                            )
                            return@launch
                        }
                    },
                    onFailure = { }
                )
                
                // Create new user
                val passwordHash = hashPassword(password)
                val result = repository.createUser(
                    email = email,
                    name = fullName,
                    phone = phone,
                    passwordHash = passwordHash
                )
                
                result.fold(
                    onSuccess = { newUser ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            registrationSuccess = true,
                            error = null
                        )
                    },
                    onFailure = { e ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Registration failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    fun clearRegistrationSuccess() {
        _authState.value = _authState.value.copy(registrationSuccess = false)
    }
    
    fun logout() {
        SettingsRepository.clearSession()
        _authState.value = AuthState()
    }
    
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        @Volatile
        private var instance: AuthViewModel? = null
        
        fun getInstance(): AuthViewModel {
            return instance ?: synchronized(this) {
                instance ?: AuthViewModel().also { instance = it }
            }
        }
    }
}
