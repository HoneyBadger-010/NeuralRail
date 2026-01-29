package com.neuralrail.neuralrailapp.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String = "user"
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float = 0.4f,
    val maxOutputTokens: Int = 1024,
    val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

/**
 * Structured response from Gemini for the Energy Analysis.
 */
@Serializable
data class GeminiAnalysisResult(
    @SerialName("isValid") val isValid: Boolean,
    @SerialName("classification") val classification: String, // VALID, FALSE, JUSTIFIED
    @SerialName("priority") val priority: String, // HIGH, MEDIUM, LOW
    @SerialName("explanation") val explanation: String,
    @SerialName("recommendedAction") val recommendedAction: String
)
