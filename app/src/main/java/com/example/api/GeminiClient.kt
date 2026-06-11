package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun parseAndCallGemini(prompt: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.trim().isEmpty()) {
            return null // Falling back to local algorithm gracefully
        }

        val requestPrompt = """
            You are Gancode AI Engine - Cloud System Engine. Your role is to convert a free-text system idea into a complete, structured software architecture blueprint.
            Construct a valid raw JSON object matching this schema. Avoid markdown wrapper backticks except raw JSON, or return raw JSON.
            
            The JSON schema:
            {
              "name": "Creative short name of the application matching user idea",
              "subdomain": "lowercase-url-friendly-slug-for-subdomain",
              "framework": "Node.js / Express" or "Python / FastAPI",
              "endpoints": [
                {
                  "method": "GET" or "POST" or "PUT" or "DELETE",
                  "path": "/api/*",
                  "desc": "Short description of endpoint purpose",
                  "sampleResponse": "{\\"status\\": \\"success\\"}"
                }
              ],
              "schema": [
                {
                  "table": "table_name",
                  "columns": [
                    {"name": "id", "type": "SERIAL PRIMARY KEY"},
                    {"name": "column", "type": "VARCHAR"}
                  ]
                }
              ],
              "pages": [
                {
                  "name": "Home Page",
                  "route": "/",
                  "components": "Header, MainDashboard, ActionsMenu",
                  "desc": "Overview of what this React page displays"
                }
              ],
              "env": {
                "PORT": "3000",
                "DATABASE_URL": "postgresql://..."
              },
              "decisions": [
                "Decision line 1",
                "Decision line 2"
              ]
            }
            
            User system idea: "$prompt"
            Respond with raw JSON only. Do not output conversational text or general explanations.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = requestPrompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
