package com.neuralrail.neuralrailapp.data.remote

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object SupabaseConfig {
    const val SUPABASE_URL = "https://abhnbeotduolcyqppodw.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiaG5iZW90ZHVvbGN5cXBwb2R3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxMjc4MDksImV4cCI6MjA4MDcwMzgwOX0.4I8XR0se84u5BsvArGvsTKkft7MYlKMXuet0DXPGHDA"
    const val REST_URL = "$SUPABASE_URL/rest/v1"
    const val AUTH_URL = "$SUPABASE_URL/auth/v1"
}

object SupabaseClient {
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }
    
    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.BODY
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        
        defaultRequest {
            url.takeFrom(SupabaseConfig.REST_URL)
            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            contentType(ContentType.Application.Json)
        }
    }
}
