package com.learner.lm.billing

data class MessageQuotaStatus(
    val usedToday: Int,
    val isPremium: Boolean,
    val canSend: Boolean,
    val remainingMessages: Int?,
    val quotaLabel: String,
    val maxMessageLength: Int
) {
    companion object {
        fun forTier(usedToday: Int, tierName: String): MessageQuotaStatus {
            val premium = tierName == SubscriptionTier.BASIC.name ||
                tierName == SubscriptionTier.PRO.name
            return MessageQuotaStatus(
                usedToday = usedToday,
                isPremium = premium,
                canSend = MessageQuotaPolicy.canSend(usedToday, premium),
                remainingMessages = MessageQuotaPolicy.remainingMessages(usedToday, premium),
                quotaLabel = MessageQuotaPolicy.quotaLabel(usedToday, premium),
                maxMessageLength = MessageQuotaPolicy.maxMessageLength(premium)
            )
        }
    }
}
