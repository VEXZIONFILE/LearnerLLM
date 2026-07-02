package com.learner.lm.repository

import android.content.Context
import com.learner.lm.billing.MessageQuotaExceededException
import com.learner.lm.billing.MessageQuotaStatus
import com.learner.lm.billing.SubscriptionTier
import retrofit2.HttpException

class MessageQuotaRepository(@Suppress("UNUSED_PARAMETER") context: Context) {

    private val apiService: LearnerApiService? by lazy {
        if (!LearnerApiConfig.isConfigured) return@lazy null
        try {
            LearnerApiClient.createService()
        } catch (_: Exception) {
            null
        }
    }

    fun isPremiumTier(tierName: String): Boolean =
        tierName == SubscriptionTier.BASIC.name || tierName == SubscriptionTier.PRO.name

    suspend fun fetchStatus(tierName: String): Result<MessageQuotaStatus> {
        val service = apiService
            ?: return Result.failure(
                IllegalStateException(
                    "Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."
                )
            )

        return try {
            val dto = service.getMessageQuota()
            Result.success(dto.toStatus())
        } catch (error: Exception) {
            Result.failure(
                IllegalStateException(
                    "Could not load message quota. Check your connection and try again.",
                    error
                )
            )
        }
    }

    fun mapHttpError(error: HttpException): Exception = when (error.code()) {
        429 -> MessageQuotaExceededException()
        400 -> IllegalArgumentException(
            error.message() ?: "Message could not be sent."
        )
        else -> IllegalStateException(
            error.message() ?: "Could not send message. Check your connection and try again."
        )
    }

    private fun MessageQuotaResponseDto.toStatus(): MessageQuotaStatus = MessageQuotaStatus(
        usedToday = used_today,
        isPremium = is_premium,
        canSend = can_send,
        remainingMessages = remaining,
        quotaLabel = quota_label,
        maxMessageLength = max_message_length
    )
}
