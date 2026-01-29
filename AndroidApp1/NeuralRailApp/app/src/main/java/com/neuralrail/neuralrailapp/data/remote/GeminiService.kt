package com.neuralrail.neuralrailapp.data.remote

import android.util.Log
import com.neuralrail.neuralrailapp.data.models.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class GeminiService(
    private val client: HttpClient,
    private val apiKey: String
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeReport(description: String, location: String): GeminiAnalysisResult {
        val prompt = constructPrompt(description, location)
        
        Log.d("GeminiService", "Sending prompt to Gemini...")

        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        try {
            val response: GeminiResponse = client.post("$baseUrl?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (textResult == null) {
                Log.e("GeminiService", "Empty response from Gemini: $response")
                return getFallbackResult()
            }

            Log.d("GeminiService", "Gemini Raw Response: $textResult")
            
            return try {
                json.decodeFromString<GeminiAnalysisResult>(textResult)
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse JSON", e)
                getFallbackResult()
            }

        } catch (e: Exception) {
            Log.e("GeminiService", "Network Error", e)
            return getFallbackResult() // Graceful degradation
        }
    }

    private fun constructPrompt(description: String, location: String): String {
        return """
            You are an AI assistant for the Indian Railways Energy Management System.
            Your role is to VALIDATE and CLASSIFY user reports of potential energy wastage.
            
            CONTEXT:
            Indian Railways wants to reduce non-traction energy waste (lights/fans left on, ACs running in empty rooms, etc.).
            You do NOT control any hardware. You ONLY provide decision support.
            
            INPUT REPORT:
            Description: "$description"
            Location: "$location"
            
            INSTRUCTIONS:
            1. Analyze if the report describes a valid energy wastage scenario.
            2. Classify it as:
               - "VALID": Genuine waste (e.g., lights on in empty platform waiting room).
               - "FALSE": Not waste or misunderstood (e.g., station lights at night are required for safety).
               - "JUSTIFIED": Usage is necessary (e.g., AC in server room).
            3. Assign Priority: HIGH (Large waste/Safety risk), MEDIUM, LOW.
            4. Provide a SHORT explanation (max 1 sentence).
            5. Recommend a human action (e.g., "Staff to check switch board").
            
            SAFETY GUIDELINES:
            - If details are vague, classify as LOW priority or FALSE.
            - Always prioritize SAFETY. Lighting on platforms at night is NOT waste.
            
            RESPONSE FORMAT:
            You must respond with ONLY valid JSON matching this schema:
            {
              "isValid": boolean,
              "classification": "VALID" | "FALSE" | "JUSTIFIED",
              "priority": "HIGH" | "MEDIUM" | "LOW",
              "explanation": "string",
              "recommendedAction": "string"
            }
        """.trimIndent()
    }

    private fun getFallbackResult(): GeminiAnalysisResult {
        return GeminiAnalysisResult(
            isValid = false,
            classification = "FALSE",
            priority = "LOW",
            explanation = "AI analysis failed. Please verify manually.",
            recommendedAction = "Manual inspection required."
        )
    }
}
