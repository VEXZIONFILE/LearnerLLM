package com.learner.lm.repository

import android.content.Context
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.FreeModelVariant
import com.learner.lm.billing.MessageQuotaExceededException
import com.learner.lm.billing.MessageQuotaStatus
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

    suspend fun fetchStatus(
        appMode: AppMode,
        freeModelVariant: FreeModelVariant
    ): Result<MessageQuotaStatus> {
        val service = apiService
            ?: return Result.failure(
                IllegalStateException(
                    "Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."
                )
            )

        return try {
            val dto = service.getMessageQuota(
                appMode = appMode.name,
                freeModelVariant = freeModelVariant.name
            )
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
        429 -> MessageQuotaExceededException(
            error.message() ?: "Daily message limit reached. Upgrade for more messages."
        )
        else -> IllegalStateException(
            error.message() ?: "Could not send message. Check your connection and try again."
        )
    }

    private fun MessageQuotaResponseDto.toStatus(): MessageQuotaStatus {
        val variant = free_model_variant
            ?.let { name -> FreeModelVariant.entries.firstOrNull { it.name == name } }
            ?: FreeModelVariant.TUTOR
        return MessageQuotaStatus(
            usedToday = used_today,
            isPremium = is_premium,
            canSend = can_send,
            remainingMessages = remaining,
            quotaLabel = quota_label,
            appMode = AppMode.entries.firstOrNull { it.name == app_mode } ?: AppMode.TUTOR,
            freeModelVariant = variant
        )
    }
}
