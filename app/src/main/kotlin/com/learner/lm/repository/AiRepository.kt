package com.learner.lm.repository

import com.learner.lm.ai.AppMode
import com.learner.lm.ai.ModelRegistry
import com.learner.lm.ai.ModelRoute
import com.learner.lm.billing.SubscriptionTier

/**
 * Legacy local AI client — production chat flows through [LearnerChatRepository] and the backend API.
 * Kept for unit tests and offline fallback messaging.
 */
class AiRepository {
    suspend fun generateResponse(
        systemPrompt: String,
        userPrompt: String,
        route: ModelRoute
    ): String = offlineFallbackResponse(route)

    suspend fun generateTutorResponse(systemPrompt: String, userPrompt: String): String =
        generateResponse(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            route = ModelRegistry.resolve(AppMode.TUTOR, SubscriptionTier.FREE.name)
        )

    private fun offlineFallbackResponse(route: ModelRoute): String = when {
        route.modelId.contains("laguna", ignoreCase = true) -> """
            I'm in Code Help mode but can't reach the API right now.

            Share the specific function or error message you're stuck on, and we'll debug it together in small steps.
        """.trimIndent()

        route.modelId.contains("nemotron", ignoreCase = true) -> """
            ## Summary
            Study mode is offline — connect to the LearnerLM backend to generate study packs.

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
