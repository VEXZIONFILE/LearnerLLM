package com.learner.lm.repository

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class UserProfileDto(
    val uid: String,
    val email: String,
    val display_name: String,
    val photo_url: String? = null,
    val grade_level: Int,
    val subscription_tier: String,
    val created_at: String
)

data class UpdateProfileRequestDto(
    val grade_level: Int? = null,
    val display_name: String? = null
)

data class StudySubjectInputDto(
    val kind: String,
    val builtin: String? = null,
    val custom_id: Long? = null
)

data class ChatMessageInputDto(
    val role: String,
    val content: String
)

data class ChatRequestDto(
    val session_id: String? = null,
    val grade_level: Int,
    val app_mode: String,
    val hint_level: Int,
    val subject: StudySubjectInputDto? = null,
    val student_message: String,
    val conversation_history: List<ChatMessageInputDto> = emptyList(),
    val scanned_text: String? = null
)

data class ChatResponseDto(
    val session_id: String,
    val message: String,
    val hint_level: Int,
    val subject_key: String,
    val subject_display_name: String,
    val model_label: String,
    val detected_mistake: String? = null,
    val encourages_attempt: Boolean = true
)

data class ScanQuotaResponseDto(
    val used_today: Int,
    val daily_limit: Int? = null,
    val remaining: Int? = null,
    val is_premium: Boolean,
    val can_scan: Boolean,
    val quota_label: String
)

data class BillingVerifyRequestDto(
    val product_id: String,
    val purchase_token: String,
    val package_name: String? = null
)

data class BillingVerifyResponseDto(
    val verified: Boolean,
    val subscription_tier: String,
    val product_id: String
)

data class CustomSubjectResponseDto(
    val id: Long,
    val name: String,
    val category: String,
    val emoji: String,
    val storage_key: String
)

data class CreateCustomSubjectRequestDto(
    val name: String,
    val category: String,
    val emoji: String? = null
)

data class ProgressResponseDto(
    val topics: List<StudyTopicDto>,
    val streak: LearningStreakDto,
    val weak_topics: List<StudyTopicDto>
)

data class StudyTopicDto(
    val id: Long,
    val name: String,
    val subject_key: String,
    val strength_score: Float,
    val last_studied_at: String
)

data class LearningStreakDto(
    val current_streak: Int,
    val longest_streak: Int,
    val last_active_date: String
)

interface LearnerApiService {
    @GET("v1/me")
    suspend fun getMe(): UserProfileDto

    @PATCH("v1/me")
    suspend fun updateMe(@Body body: UpdateProfileRequestDto): UserProfileDto

    @POST("v1/chat/messages")
    suspend fun sendChatMessage(@Body body: ChatRequestDto): ChatResponseDto

    @GET("v1/scans/quota")
    suspend fun getScanQuota(): ScanQuotaResponseDto

    @POST("v1/scans")
    suspend fun recordScan(): ScanQuotaResponseDto

    @POST("v1/billing/verify")
    suspend fun verifyPurchase(@Body body: BillingVerifyRequestDto): BillingVerifyResponseDto

    @GET("v1/subjects")
    suspend fun listSubjects(): List<CustomSubjectResponseDto>

    @POST("v1/subjects")
    suspend fun createSubject(@Body body: CreateCustomSubjectRequestDto): CustomSubjectResponseDto

    @DELETE("v1/subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: Long)

    @GET("v1/progress")
    suspend fun getProgress(): ProgressResponseDto
}
