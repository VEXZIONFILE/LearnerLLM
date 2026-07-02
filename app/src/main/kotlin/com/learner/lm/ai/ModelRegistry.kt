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

    const val FREE_TUTOR_MODEL = "openai/gpt-oss-120b:free"
    const val FREE_STUDY_MODEL = "nvidia/nemotron-3-super-120b-a12b:free"
    const val FREE_CODE_MODEL = "poolside/laguna-m.1:free"

    fun resolve(
        mode: AppMode,
        subscriptionTier: String,
        freeModelVariant: FreeModelVariant = FreeModelVariant.TUTOR
    ): ModelRoute {
        if (subscriptionTier == SubscriptionTier.FREE.name) {
            return resolveFreeVariant(freeModelVariant)
        }

        val capabilities = SubscriptionCapabilities.forTier(subscriptionTier)
        return when (mode) {
            AppMode.TUTOR, AppMode.FREE -> ModelRoute(
                modelId = TUTOR_MODEL,
                displayName = "Learner Tutor",
                temperature = when {
                    capabilities.isPro -> 0.75
                    capabilities.isPremium -> 0.7
                    else -> 0.65
                },
                maxTokens = capabilities.tutorMaxTokens
            )
            AppMode.STUDY -> ModelRoute(
                modelId = STUDY_MODEL,
                displayName = "Learner Study",
                temperature = when {
                    capabilities.isPro -> 0.55
                    capabilities.isPremium -> 0.5
                    else -> 0.45
                },
                maxTokens = capabilities.studyMaxTokens
            )
            AppMode.CODE -> ModelRoute(
                modelId = CODE_MODEL,
                displayName = "Learner Code",
                temperature = when {
                    capabilities.isPro -> 0.45
                    capabilities.isPremium -> 0.4
                    else -> 0.35
                },
                maxTokens = capabilities.codeMaxTokens
            )
        }
    }

    private fun resolveFreeVariant(variant: FreeModelVariant): ModelRoute {
        val capabilities = SubscriptionCapabilities.forTier(SubscriptionTier.FREE.name)
        return when (variant) {
            FreeModelVariant.TUTOR -> ModelRoute(
                modelId = FREE_TUTOR_MODEL,
                displayName = variant.displayName,
                temperature = 0.65,
                maxTokens = capabilities.tutorMaxTokens
            )
            FreeModelVariant.STUDY -> ModelRoute(
                modelId = FREE_STUDY_MODEL,
                displayName = variant.displayName,
                temperature = 0.45,
                maxTokens = capabilities.studyMaxTokens
            )
            FreeModelVariant.CODE -> ModelRoute(
                modelId = FREE_CODE_MODEL,
                displayName = variant.displayName,
                temperature = 0.35,
                maxTokens = capabilities.codeMaxTokens
            )
        }
    }

    fun displayLabel(
        mode: AppMode,
        subscriptionTier: String,
        freeModelVariant: FreeModelVariant = FreeModelVariant.TUTOR
    ): String = resolve(mode, subscriptionTier, freeModelVariant).displayName
}
