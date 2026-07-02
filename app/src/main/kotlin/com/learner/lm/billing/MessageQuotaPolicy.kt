package com.learner.lm.billing

import com.learner.lm.ai.AppMode
import com.learner.lm.ai.FreeModelVariant

object MessageQuotaPolicy {
    const val FREE_DAILY_LIMIT_PER_MODEL = 60
    const val PRO_DAILY_LIMIT_PER_MODE = 500

    fun dailyLimit(tierName: String): Int? = when (tierName) {
        SubscriptionTier.PRO.name -> null
        SubscriptionTier.BASIC.name -> PRO_DAILY_LIMIT_PER_MODE
        else -> FREE_DAILY_LIMIT_PER_MODEL
    }

    fun canSend(usedToday: Int, tierName: String): Boolean {
        val limit = dailyLimit(tierName) ?: return true
        return usedToday < limit
    }

    fun remainingMessages(usedToday: Int, tierName: String): Int? {
        val limit = dailyLimit(tierName) ?: return null
        return (limit - usedToday).coerceAtLeast(0)
    }

    fun quotaLabel(
        usedToday: Int,
        tierName: String,
        appMode: AppMode,
        freeModelVariant: FreeModelVariant
    ): String {
        val limit = dailyLimit(tierName)
        val bucketLabel = bucketLabel(tierName, appMode, freeModelVariant)
        return if (limit == null) {
            "Unlimited messages with $bucketLabel"
        } else {
            val remaining = remainingMessages(usedToday, tierName) ?: 0
            "$remaining of $limit $bucketLabel messages left today"
        }
    }

    fun bucketLabel(
        tierName: String,
        appMode: AppMode,
        freeModelVariant: FreeModelVariant
    ): String = if (tierName == SubscriptionTier.FREE.name) {
        freeModelVariant.quotaLabel
    } else {
        "${modeLabel(appMode)} mode"
    }

    fun modeLabel(appMode: AppMode): String = when (appMode) {
        AppMode.TUTOR -> "Tutor"
        AppMode.STUDY -> "Study"
        AppMode.CODE -> "Code"
        AppMode.FREE -> "Free"
    }
}

class MessageQuotaExceededException(
    message: String = "Daily message limit reached. Upgrade for more messages."
) : IllegalStateException(message)
