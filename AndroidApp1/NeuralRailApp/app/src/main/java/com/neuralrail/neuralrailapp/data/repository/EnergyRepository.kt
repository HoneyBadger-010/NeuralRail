package com.neuralrail.neuralrailapp.data.repository

import com.neuralrail.neuralrailapp.data.models.WastageReport
import com.neuralrail.neuralrailapp.data.models.GeminiAnalysisResult
import com.neuralrail.neuralrailapp.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

interface EnergyRepository {
    suspend fun analyzeReport(description: String, location: String): GeminiAnalysisResult
    suspend fun submitReport(report: WastageReport): Result<Boolean>
}

class EnergyRepositoryImpl(
    private val geminiService: GeminiService,
    private val firestore: FirebaseFirestore? = null
) : EnergyRepository {

    override suspend fun analyzeReport(description: String, location: String): GeminiAnalysisResult {
        return geminiService.analyzeReport(description, location)
    }

    override suspend fun submitReport(report: WastageReport): Result<Boolean> {
        if (firestore == null) {
            Log.w("EnergyRepo", "Firestore not available, skipping submission for report: ${report.id}")
            return Result.success(true) // Pretend success for demo
        }

        return try {
            // Ensure ID is generated if missing
            val reportId = if (report.id.isBlank()) UUID.randomUUID().toString() else report.id
            val finalReport = report.copy(id = reportId)

            // Save to "energy_reports" collection
            firestore.collection("energy_reports")
                .document(reportId)
                .set(finalReport)
                .await()
            
            Log.d("EnergyRepo", "Report submitted successfully: $reportId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("EnergyRepo", "Error submitting to Firestore", e)
            Result.failure(e)
        }
    }
}

/**
 * Mock implementation for testing without Firebase/Internet
 */
class MockEnergyRepository : EnergyRepository {
    override suspend fun analyzeReport(description: String, location: String): GeminiAnalysisResult {
        kotlinx.coroutines.delay(1500) // Simulate network delay
        return GeminiAnalysisResult(
            isValid = true,
            classification = "VALID",
            priority = "MEDIUM",
            explanation = "Lights on in an empty area is a valid waste of energy.",
            recommendedAction = "Request station manager to automate lighting schedule."
        )
    }

    override suspend fun submitReport(report: WastageReport): Result<Boolean> {
        kotlinx.coroutines.delay(1000)
        return Result.success(true)
    }
}
