package com.learner.lm.ai

import com.learner.lm.billing.ScanQuotaPolicy
import com.learner.lm.billing.SubscriptionTier

/**
 * Controls output depth and richness by subscription tier.
 * Correctness and educational integrity rules never change — only depth/detail.
 */
data class SubscriptionCapabilities(
    val isPremium: Boolean,
    val isPro: Boolean,
    val tutorMaxTokens: Int,
    val studyMaxTokens: Int,
    val codeMaxTokens: Int,
    val studySections: StudySectionDepth,
    val tutorExampleCount: Int,
    val codeMaxSuggestedLines: Int,
    val conversationHistoryLimit: Int,
    val dailyHomeworkScans: Int?
) {
    enum class StudySectionDepth {
        BASIC,
        FULL,
        PRO
    }

    companion object {
        fun forTier(tierName: String): SubscriptionCapabilities {
            return when (tierName) {
                SubscriptionTier.PRO.name -> SubscriptionCapabilities(
                    isPremium = true,
                    isPro = true,
                    tutorMaxTokens = 3_072,
                    studyMaxTokens = 4_096,
                    codeMaxTokens = 3_072,
                    studySections = StudySectionDepth.PRO,
                    tutorExampleCount = 5,
                    codeMaxSuggestedLines = 60,
                    conversationHistoryLimit = 16,
                    dailyHomeworkScans = null
                )
                SubscriptionTier.BASIC.name -> SubscriptionCapabilities(
                    isPremium = true,
                    isPro = false,
                    tutorMaxTokens = 2_048,
                    studyMaxTokens = 3_072,
                    codeMaxTokens = 2_048,
                    studySections = StudySectionDepth.FULL,
                    tutorExampleCount = 3,
                    codeMaxSuggestedLines = 40,
                    conversationHistoryLimit = 12,
                    dailyHomeworkScans = null
                )
                else -> SubscriptionCapabilities(
                    isPremium = false,
                    isPro = false,
                    tutorMaxTokens = 1_024,
                    studyMaxTokens = 1_536,
                    codeMaxTokens = 1_024,
                    studySections = StudySectionDepth.BASIC,
                    tutorExampleCount = 1,
                    codeMaxSuggestedLines = 20,
                    conversationHistoryLimit = 8,
                    dailyHomeworkScans = ScanQuotaPolicy.FREE_DAILY_LIMIT
                )
            }
        }
    }
}
