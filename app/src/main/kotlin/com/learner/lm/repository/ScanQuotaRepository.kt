package com.learner.lm.repository

import android.content.Context
import com.learner.lm.billing.ScanQuotaExceededException
import com.learner.lm.billing.ScanQuotaStatus
import com.learner.lm.billing.SubscriptionTier
import retrofit2.HttpException

/**
 * Tracks homework scan usage via the LearnerLM backend API.
 * Server enforces daily limits atomically per authenticated user.
 */
class ScanQuotaRepository(@Suppress("UNUSED_PARAMETER") context: Context) {

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

    suspend fun fetchStatus(userId: String, tierName: String): Result<ScanQuotaStatus> {
        if (userId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to scan homework."))
        }
        val service = apiService
            ?: return Result.failure(IllegalStateException("Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."))

        return try {
            val dto = service.getScanQuota()
            Result.success(dto.toStatus())
        } catch (error: Exception) {
            Result.failure(
                IllegalStateException(
                    "Could not load scan quota. Check your connection and try again.",
                    error
                )
            )
        }
    }

    suspend fun recordSuccessfulScan(userId: String, tierName: String): Result<ScanQuotaStatus> {
        if (userId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to scan homework."))
        }
        val service = apiService
            ?: return Result.failure(IllegalStateException("Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."))

        return try {
            val dto = service.recordScan()
            Result.success(dto.toStatus())
        } catch (error: HttpException) {
            if (error.code() == 429) {
                Result.failure(ScanQuotaExceededException())
            } else {
                Result.failure(
                    IllegalStateException(
                        "Could not save scan usage. Check your connection and try again.",
                        error
                    )
                )
            }
        } catch (error: Exception) {
            Result.failure(
                IllegalStateException(
                    "Could not save scan usage. Check your connection and try again.",
                    error
                )
            )
        }
    }

    private fun ScanQuotaResponseDto.toStatus(): ScanQuotaStatus = ScanQuotaStatus(
        usedToday = used_today,
        isPremium = is_premium,
        canScan = can_scan,
        remainingScans = remaining,
        quotaLabel = quota_label
    )
}
