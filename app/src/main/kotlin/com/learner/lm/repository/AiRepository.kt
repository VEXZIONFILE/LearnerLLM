package com.learner.lm.repository

import com.learner.lm.ai.AiConfig
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.ModelRoute
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
    suspend fun generateResponse(
        systemPrompt: String,
        userPrompt: String,
        route: ModelRoute
    ): String {
        val apiKey = apiKeyProvider()?.takeIf { it.isNotBlank() }
            ?: return offlineFallbackResponse(route)

        return try {
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                referer = AiConfig.appReferer,
                title = AiConfig.MODEL_DISPLAY_NAME,
                request = ChatCompletionRequest(
                    model = route.modelId,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(role = "user", content = userPrompt)
                    ),
                    temperature = route.temperature,
                    max_tokens = route.maxTokens
                )
            )
            response.choices.firstOrNull()?.message?.content
                ?: offlineFallbackResponse(route)
        } catch (_: Exception) {
            offlineFallbackResponse(route)
        }
    }

    /** @deprecated Use [generateResponse] with [ModelRoute] */
    suspend fun generateTutorResponse(systemPrompt: String, userPrompt: String): String =
        generateResponse(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            route = com.learner.lm.ai.ModelRegistry.resolve(
                AppMode.TUTOR,
                com.learner.lm.billing.SubscriptionTier.FREE.name
            )
        )

    private fun offlineFallbackResponse(route: ModelRoute): String = when {
        route.modelId.contains("laguna", ignoreCase = true) -> """
            I'm in Code Help mode but can't reach the API right now.

            Share the specific function or error message you're stuck on, and we'll debug it together in small steps.
        """.trimIndent()

        route.modelId.contains("nemotron", ignoreCase = true) -> """
            ## Summary
            Study mode is offline — add your OpenRouter API key in local.properties to generate study packs.

            ## Key Concepts
            - Review your class notes on this topic
            - Identify vocabulary you don't know yet

            ## Flashcards
            Q: What is the main idea of your topic?
            A: (Write your answer from your notes)
        """.trimIndent()

        else -> """
            I'm here to help you think through this step by step.

            Let's start with what you already know about the problem. Can you tell me:
            1. What is the problem asking you to find?
            2. What information have you been given?
            3. What approach might you try first?

            Remember — I'm not going to give you the answer. My job is to help you discover it yourself!
        """.trimIndent()
    }
}
