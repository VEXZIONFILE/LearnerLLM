package com.learner.lm.billing

import com.learner.lm.ai.AppMode
import com.learner.lm.ai.FreeModelVariant

data class MessageQuotaStatus(
    val usedToday: Int,
    val isPremium: Boolean,
    val canSend: Boolean,
    val remainingMessages: Int?,
    val quotaLabel: String,
    val appMode: AppMode,
    val freeModelVariant: FreeModelVariant
) {
    companion object {
        fun forTier(
            usedToday: Int,
            tierName: String,
            appMode: AppMode,
            freeModelVariant: FreeModelVariant = FreeModelVariant.defaultForMode(appMode)
        ): MessageQuotaStatus {
            val unlimited = MessageQuotaPolicy.dailyLimit(tierName) == null
            return MessageQuotaStatus(
                usedToday = usedToday,
                isPremium = unlimited,
                canSend = MessageQuotaPolicy.canSend(usedToday, tierName),
                remainingMessages = MessageQuotaPolicy.remainingMessages(usedToday, tierName),
                quotaLabel = MessageQuotaPolicy.quotaLabel(usedToday, tierName, appMode, freeModelVariant),
                appMode = appMode,
                freeModelVariant = freeModelVariant
            )
        }
    }
}
