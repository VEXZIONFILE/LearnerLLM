package com.learner.lm.ai

import com.learner.lm.billing.ScanQuotaPolicy
import com.learner.lm.billing.SubscriptionTier

/**
 * Controls output depth and richness by subscription tier.
 * Correctness and educational integrity rules never change — only depth/detail.
 */
data class SubscriptionCapabilities(
    val isPremium: Boolean,
    val tutorMaxTokens: Int,
    val studyMaxTokens: Int,
    val codeMaxTokens: Int,
    val studySections: StudySectionDepth,
    val tutorExampleCount: Int,
    val codeMaxSuggestedLines: Int,
    val dailyHomeworkScans: Int?
) {
    enum class StudySectionDepth {
        BASIC,
        FULL
    }

    companion object {
        fun forTier(tierName: String): SubscriptionCapabilities {
            val isPremium = tierName == SubscriptionTier.BASIC.name ||
                tierName == SubscriptionTier.PRO.name
            return if (isPremium) {
                SubscriptionCapabilities(
                    isPremium = true,
                    tutorMaxTokens = 2_048,
                    studyMaxTokens = 3_072,
                    codeMaxTokens = 2_048,
                    studySections = StudySectionDepth.FULL,
                    tutorExampleCount = 3,
                    codeMaxSuggestedLines = 40,
                    dailyHomeworkScans = null
                )
            } else {
                SubscriptionCapabilities(
                    isPremium = false,
                    tutorMaxTokens = 1_024,
                    studyMaxTokens = 1_536,
                    codeMaxTokens = 1_024,
                    studySections = StudySectionDepth.BASIC,
                    tutorExampleCount = 1,
                    codeMaxSuggestedLines = 20,
                    dailyHomeworkScans = ScanQuotaPolicy.FREE_DAILY_LIMIT
                )
            }
        }
    }
}
