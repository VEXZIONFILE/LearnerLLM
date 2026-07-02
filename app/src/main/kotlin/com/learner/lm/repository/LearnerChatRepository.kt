package com.learner.lm.repository

import com.learner.lm.billing.MessageQuotaExceededException
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.ai.AiReportReason
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.HintLevel
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.TutorContext
import com.learner.lm.ai.TutorResponse
import retrofit2.HttpException
import java.io.IOException

class LearnerChatRepository(
    private val apiService: LearnerApiService = LearnerApiClient.createService()
) {
    suspend fun sendMessage(
        sessionId: String,
        context: TutorContext
    ): TutorResponse {
        val subjectInput = when (val subject = context.subject) {
            is StudySubject.Builtin -> StudySubjectInputDto(
                kind = "builtin",
                builtin = subject.subject.name
            )
            is StudySubject.Custom -> StudySubjectInputDto(
                kind = "custom",
                custom_id = subject.id
            )
        }

        val history = context.conversationHistory.map { (role, content) ->
            ChatMessageInputDto(role = role, content = content)
        }

        return try {
            val response = apiService.sendChatMessage(
                ChatRequestDto(
                    session_id = sessionId,
                    grade_level = context.gradeLevel,
                    app_mode = context.appMode.name,
                    free_model_variant = if (context.subscriptionTier == SubscriptionTier.FREE.name) {
                        context.freeModelVariant.name
                    } else {
                        null
                    },
                    hint_level = context.hintLevel.level,
                    subject = subjectInput,
                    student_message = context.studentMessage,
                    conversation_history = history,
                    scanned_text = context.scannedText
                )
            )
            TutorResponse(
                message = response.message,
                hintLevel = HintLevel.entries.firstOrNull { it.level == response.hint_level }
                    ?: context.hintLevel,
                subject = StudySubject.fromStorageKey(response.subject_key, context.subject.let {
                    if (it is StudySubject.Custom) listOf(it) else emptyList()
                }),
                detectedMistake = response.detected_mistake,
                encouragesAttempt = response.encourages_attempt
            )
        } catch (error: HttpException) {
            throw mapChatHttpError(error)
        } catch (error: IOException) {
            throw IOException(NetworkErrors.friendlyMessage(error), error)
        }
    }

    suspend fun reportContent(
        sessionId: String,
        messageId: Long,
        content: String,
        reason: AiReportReason,
        details: String?,
        appMode: AppMode
    ): ReportContentResponseDto {
        return apiService.reportContent(
            ReportContentRequestDto(
                session_id = sessionId,
                message_id = messageId,
                content = content,
                reason = reason.apiValue,
                details = details,
                app_mode = appMode.name
            )
        )
    }

    private fun mapChatHttpError(error: HttpException): Exception = when (error.code()) {
        429 -> MessageQuotaExceededException(NetworkErrors.friendlyMessage(error))
        else -> IllegalStateException(NetworkErrors.friendlyMessage(error))
    }
}
