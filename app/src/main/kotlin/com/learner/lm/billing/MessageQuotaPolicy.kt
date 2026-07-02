package com.learner.lm.billing

object MessageQuotaPolicy {
    const val FREE_DAILY_LIMIT = 25
    const val FREE_MAX_MESSAGE_LENGTH = 2000
    const val PREMIUM_MAX_MESSAGE_LENGTH = 8000

    fun canSend(usedToday: Int, isPremium: Boolean): Boolean =
        isPremium || usedToday < FREE_DAILY_LIMIT

    fun remainingMessages(usedToday: Int, isPremium: Boolean): Int? =
        if (isPremium) null else (FREE_DAILY_LIMIT - usedToday).coerceAtLeast(0)

    fun quotaLabel(usedToday: Int, isPremium: Boolean): String =
        if (isPremium) {
            "Unlimited messages"
        } else {
            "${remainingMessages(usedToday, isPremium)} of $FREE_DAILY_LIMIT messages left today"
        }

    fun maxMessageLength(isPremium: Boolean): Int =
        if (isPremium) PREMIUM_MAX_MESSAGE_LENGTH else FREE_MAX_MESSAGE_LENGTH
}

class MessageQuotaExceededException : IllegalStateException(
    "Daily message limit reached. Upgrade to Pro for unlimited chat messages."
)
