package com.learner.lm.repository

import com.learner.lm.ai.AiConfig
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ChatCompletionRequest(
    val model: String = AiConfig.TUTOR_MODEL_ID,
    val messages: List<ChatMessageDto>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1024
)

data class ChatMessageDto(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val choices: List<ChatChoice>
)

data class ChatChoice(
    val message: ChatMessageDto
)

interface AiApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
