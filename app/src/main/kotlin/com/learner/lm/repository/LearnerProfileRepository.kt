package com.learner.lm.repository

import com.learner.lm.auth.UserProfile

class LearnerProfileRepository(
    private val apiService: LearnerApiService = LearnerApiClient.createService()
) {
    suspend fun fetchProfile(): Result<UserProfile> = try {
        val dto = apiService.getMe()
        Result.success(
            UserProfile(
                uid = dto.uid,
                displayName = dto.display_name,
                email = dto.email,
                photoUrl = dto.photo_url,
                gradeLevel = dto.grade_level,
                subscriptionTier = dto.subscription_tier
            )
        )
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun updateGradeLevel(gradeLevel: Int): Result<Unit> = try {
        apiService.updateMe(UpdateProfileRequestDto(grade_level = gradeLevel))
        Result.success(Unit)
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun verifyPurchase(productId: String, purchaseToken: String): Result<String> = try {
        val response = apiService.verifyPurchase(
            BillingVerifyRequestDto(
                product_id = productId,
                purchase_token = purchaseToken
            )
        )
        if (response.verified) {
            Result.success(response.subscription_tier)
        } else {
            Result.failure(IllegalStateException("Purchase could not be verified"))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }
}
