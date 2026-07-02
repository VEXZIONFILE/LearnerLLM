package com.learner.lm.billing

import com.learner.lm.ai.AppMode

object MessageQuotaPolicy {
    const val FREE_DAILY_LIMIT_PER_MODE = 60
    const val PRO_DAILY_LIMIT_PER_MODE = 500

    fun dailyLimit(tierName: String): Int? = when (tierName) {
        SubscriptionTier.PRO.name -> null
        SubscriptionTier.BASIC.name -> PRO_DAILY_LIMIT_PER_MODE
        else -> FREE_DAILY_LIMIT_PER_MODE
    }

    fun canSend(usedToday: Int, tierName: String): Boolean {
        val limit = dailyLimit(tierName) ?: return true
        return usedToday < limit
    }

    fun remainingMessages(usedToday: Int, tierName: String): Int? {
        val limit = dailyLimit(tierName) ?: return null
        return (limit - usedToday).coerceAtLeast(0)
    }

    fun quotaLabel(usedToday: Int, tierName: String, appMode: AppMode): String {
        val modeLabel = modeLabel(appMode)
        val limit = dailyLimit(tierName)
        return if (limit == null) {
            "Unlimited messages in $modeLabel"
        } else {
            val remaining = remainingMessages(usedToday, tierName) ?: 0
            "$remaining of $limit $modeLabel messages left today"
        }
    }

    fun modeLabel(appMode: AppMode): String = when (appMode) {
        AppMode.TUTOR -> "Tutor"
        AppMode.STUDY -> "Study"
        AppMode.CODE -> "Code"
        AppMode.FREE -> "Free"
    }
}

class MessageQuotaExceededException(
    message: String = "Daily message limit reached for this mode. Upgrade for more messages."
) : IllegalStateException(message)
