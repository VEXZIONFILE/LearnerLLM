package com.learner.lm.repository

import com.learner.lm.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    fun createAiApiService(apiKey: String? = null): AiApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.AI_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }
}

class AiRepository(
    private val apiService: AiApiService,
    private val apiKeyProvider: () -> String?
) {
    suspend fun generateTutorResponse(systemPrompt: String, userPrompt: String): String {
        val apiKey = apiKeyProvider()?.takeIf { it.isNotBlank() }
            ?: return offlineFallbackResponse(userPrompt)

        return try {
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = ChatCompletionRequest(
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(role = "user", content = userPrompt)
                    )
                )
            )
            response.choices.firstOrNull()?.message?.content
                ?: offlineFallbackResponse(userPrompt)
        } catch (_: Exception) {
            offlineFallbackResponse(userPrompt)
        }
    }

    private fun offlineFallbackResponse(userPrompt: String): String {
        return """
            I'm here to help you think through this step by step.

            Let's start with what you already know about the problem. Can you tell me:
            1. What is the problem asking you to find?
            2. What information have you been given?
            3. What approach might you try first?

            Remember — I'm not going to give you the answer. My job is to help you discover it yourself!
        """.trimIndent()
    }
}
