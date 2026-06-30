package com.learner.lm.repository

import com.learner.lm.ai.AppMode
import com.learner.lm.ai.HintLevel
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.TutorContext
import com.learner.lm.ai.TutorResponse

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

        val response = apiService.sendChatMessage(
            ChatRequestDto(
                session_id = sessionId,
                grade_level = context.gradeLevel,
                app_mode = context.appMode.name,
                hint_level = context.hintLevel.level,
                subject = subjectInput,
                student_message = context.studentMessage,
                conversation_history = history,
                scanned_text = context.scannedText
            )
        )

        return TutorResponse(
            message = response.message,
            hintLevel = HintLevel.entries.firstOrNull { it.level == response.hint_level }
                ?: context.hintLevel,
            subject = StudySubject.fromStorageKey(response.subject_key, context.subject.let {
                if (it is StudySubject.Custom) listOf(it) else emptyList()
            }),
            detectedMistake = response.detected_mistake,
            encouragesAttempt = response.encourages_attempt
        )
    }
}
