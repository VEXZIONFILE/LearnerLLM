package com.learner.lm.ai

import com.learner.lm.billing.SubscriptionTier

data class ModelRoute(
    val modelId: String,
    val displayName: String,
    val temperature: Double,
    val maxTokens: Int
)

object ModelRegistry {

    const val TUTOR_MODEL = "openai/gpt-oss-120b"
    const val STUDY_MODEL = "nvidia/nemotron-3-super-120b-a12b"
    const val CODE_MODEL = "poolside/laguna-m.1"

    fun resolve(mode: AppMode, subscriptionTier: String): ModelRoute {
        val capabilities = SubscriptionCapabilities.forTier(subscriptionTier)
        return when (mode) {
            AppMode.TUTOR -> ModelRoute(
                modelId = TUTOR_MODEL,
                displayName = "LearnerLM Tutor",
                temperature = if (capabilities.isPremium) 0.7 else 0.65,
                maxTokens = capabilities.tutorMaxTokens
            )
            AppMode.STUDY -> ModelRoute(
                modelId = STUDY_MODEL,
                displayName = "Nemotron Study",
                temperature = if (capabilities.isPremium) 0.5 else 0.45,
                maxTokens = capabilities.studyMaxTokens
            )
            AppMode.CODE -> ModelRoute(
                modelId = CODE_MODEL,
                displayName = "Laguna Code",
                temperature = if (capabilities.isPremium) 0.4 else 0.35,
                maxTokens = capabilities.codeMaxTokens
            )
        }
    }

    fun displayLabel(mode: AppMode, subscriptionTier: String): String {
        val route = resolve(mode, subscriptionTier)
        val tier = SubscriptionCapabilities.forTier(subscriptionTier)
        val tierTag = if (tier.isPremium) "Premium" else "Free"
        return "${route.displayName} · $tierTag"
    }
}
