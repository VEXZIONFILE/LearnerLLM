package com.learner.lm.repository

import com.learner.lm.ai.AiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    fun createAiApiService(): AiApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(AiConfig.apiBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }
}

class AiRepository(
    private val apiService: AiApiService,
    private val apiKeyProvider: () -> String? = { AiConfig.apiKey.takeIf { it.isNotBlank() } }
) {
    suspend fun generateTutorResponse(systemPrompt: String, userPrompt: String): String {
        val apiKey = apiKeyProvider()?.takeIf { it.isNotBlank() }
            ?: return offlineFallbackResponse()

        return try {
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                referer = AiConfig.appReferer,
                title = AiConfig.MODEL_DISPLAY_NAME,
                request = ChatCompletionRequest(
                    model = AiConfig.MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(role = "user", content = userPrompt)
                    )
                )
            )
            response.choices.firstOrNull()?.message?.content
                ?: offlineFallbackResponse()
        } catch (_: Exception) {
            offlineFallbackResponse()
        }
    }

    private fun offlineFallbackResponse(): String {
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
