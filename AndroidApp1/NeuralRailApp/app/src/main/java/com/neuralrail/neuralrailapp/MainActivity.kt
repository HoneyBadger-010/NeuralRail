package com.neuralrail.neuralrailapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.neuralrail.neuralrailapp.data.remote.UserDto
import com.neuralrail.neuralrailapp.data.repository.MockEcoRepository
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository
import com.neuralrail.neuralrailapp.presentation.screens.AuthScreen
import com.neuralrail.neuralrailapp.presentation.screens.LanguageSelectionScreen
import com.neuralrail.neuralrailapp.presentation.screens.MainScreen
import com.neuralrail.neuralrailapp.presentation.theme.NeuralRailAppTheme
import com.neuralrail.neuralrailapp.presentation.viewmodels.AuthViewModel
import com.neuralrail.neuralrailapp.presentation.viewmodels.ViewModelFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.neuralrail.neuralrailapp.data.remote.GeminiService
import com.neuralrail.neuralrailapp.data.repository.EnergyRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SettingsRepository with context for SharedPreferences
        SettingsRepository.init(applicationContext)
        SettingsRepository.loadLanguage()
        
        // Apply saved language
        val savedLanguage = SettingsRepository.getLanguage()
        setLocale(savedLanguage)

        val ecoRepository = MockEcoRepository()
        
        // Gemini & Firestore Setup
        val httpClient = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val geminiApiKey = "AIzaSyBKdTBRhNv2Ng-cvSEP-Vu-cLBk9_5Ck0Y" // Hackathon Key
        val geminiService = GeminiService(httpClient, geminiApiKey)
        // Note: Ensure Firebase is initialized (usually auto-init via google-services plugin)
        val firestore = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Firebase/Firestore init failed (missing google-services.json?)", e)
            null
        }
        val energyRepository = EnergyRepositoryImpl(geminiService, firestore)
        
        val viewModelFactory = ViewModelFactory(ecoRepository, energyRepository, applicationContext)
        val authViewModel = AuthViewModel.getInstance()

        setContent {
            var showLanguageSelection by remember { mutableStateOf(!SettingsRepository.isLanguageSelected()) }
            var currentLanguage by remember { mutableStateOf(savedLanguage) }
            
            NeuralRailAppTheme {
                val authState by authViewModel.authState.collectAsState()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showLanguageSelection -> {
                            LanguageSelectionScreen(
                                onLanguageSelected = { languageCode ->
                                    SettingsRepository.setLanguage(languageCode)
                                    setLocale(languageCode)
                                    currentLanguage = languageCode
                                    showLanguageSelection = false
                                    recreate() // Recreate to apply new locale
                                }
                            )
                        }
                        // authState.isLoggedIn -> { // Original check
                        true -> { // BYPASS AUTH for Hackathon Demo
                            MainScreen(
                                factory = viewModelFactory,
                                currentUser = authState.currentUser,
                                onLogout = { authViewModel.logout() }
                            )
                        }
                        else -> {
                            AuthScreen(
                                onAuthSuccess = { },
                                authViewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    override fun attachBaseContext(newBase: Context) {
        val languageCode = newBase.getSharedPreferences("neuralrail_prefs", Context.MODE_PRIVATE)
            .getString("selected_language", "en") ?: "en"
        val locale = Locale(languageCode)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
