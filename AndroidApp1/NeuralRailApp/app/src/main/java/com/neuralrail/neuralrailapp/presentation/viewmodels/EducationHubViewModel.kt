package com.neuralrail.neuralrailapp.presentation.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.EducationContent
import com.neuralrail.neuralrailapp.data.models.FactOfTheDay
import com.neuralrail.neuralrailapp.data.models.QuizQuestion
import com.neuralrail.neuralrailapp.data.repository.EcoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class EducationHubViewModel(private val repository: EcoRepository, private val context: Context) : ViewModel() {
    
    private val _factState = MutableStateFlow<UiState<FactOfTheDay>>(UiState.Loading)
    val factState: StateFlow<UiState<FactOfTheDay>> = _factState.asStateFlow()
    
    private val _contentState = MutableStateFlow<UiState<List<EducationContent>>>(UiState.Loading)
    val contentState: StateFlow<UiState<List<EducationContent>>> = _contentState.asStateFlow()
    
    private val _quizState = MutableStateFlow<UiState<List<QuizQuestion>>>(UiState.Loading)
    val quizState: StateFlow<UiState<List<QuizQuestion>>> = _quizState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getFactOfTheDay().collect { _factState.value = it }
        }
        viewModelScope.launch {
            repository.getEducationContent().collect { _contentState.value = it }
        }
        viewModelScope.launch {
            // Load localized quiz questions
            _quizState.value = UiState.Success(getLocalizedQuizQuestions())
        }
    }
    
    private fun getLocalizedContext(): Context {
        // Get saved language from SharedPreferences
        val prefs = context.getSharedPreferences("neuralrail_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("selected_language", "en") ?: "en"
        
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
    
    private fun getLocalizedQuizQuestions(): List<QuizQuestion> {
        val localizedContext = getLocalizedContext()
        val questions = listOf(
            QuizQuestion(
                "1",
                localizedContext.getString(R.string.quiz_q1),
                localizedContext.resources.getStringArray(R.array.quiz_q1_options).toList(),
                1,
                localizedContext.getString(R.string.quiz_q1_explanation)
            ),
            QuizQuestion(
                "2",
                localizedContext.getString(R.string.quiz_q2),
                localizedContext.resources.getStringArray(R.array.quiz_q2_options).toList(),
                0,
                localizedContext.getString(R.string.quiz_q2_explanation)
            ),
            QuizQuestion(
                "3",
                localizedContext.getString(R.string.quiz_q3),
                localizedContext.resources.getStringArray(R.array.quiz_q3_options).toList(),
                3,
                localizedContext.getString(R.string.quiz_q3_explanation)
            ),
            QuizQuestion(
                "4",
                localizedContext.getString(R.string.quiz_q4),
                localizedContext.resources.getStringArray(R.array.quiz_q4_options).toList(),
                1,
                localizedContext.getString(R.string.quiz_q4_explanation)
            ),
            QuizQuestion(
                "5",
                localizedContext.getString(R.string.quiz_q5),
                localizedContext.resources.getStringArray(R.array.quiz_q5_options).toList(),
                0,
                localizedContext.getString(R.string.quiz_q5_explanation)
            )
        )
        return questions
    }
}
