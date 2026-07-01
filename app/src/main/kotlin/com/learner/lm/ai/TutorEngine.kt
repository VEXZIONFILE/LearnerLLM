package com.learner.lm.ai

import com.learner.lm.repository.AiRepository

class TutorEngine(
    private val aiRepository: AiRepository,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val subjectClassifier: SubjectClassifier = SubjectClassifier()
) {

    suspend fun respond(context: TutorContext): TutorResponse {
        val subject = resolveSubject(context)
        val enriched = context.copy(subject = subject)
        val route = ModelRegistry.resolve(
            enriched.appMode,
            enriched.subscriptionTier,
            enriched.freeModelVariant
        )

        val systemPrompt = promptBuilder.buildSystemPrompt(enriched)
        val userPrompt = promptBuilder.buildUserPrompt(enriched)

        val message = aiRepository.generateResponse(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            route = route
        )

        val behaviorMode = enriched.appMode.learningBehavior(enriched.freeModelVariant)
        val sanitized = sanitizeResponse(message, behaviorMode)
        val nextHintLevel = when (behaviorMode) {
            AppMode.TUTOR -> determineNextHintLevel(enriched, sanitized)
            else -> enriched.hintLevel
        }

        return TutorResponse(
            message = sanitized,
            hintLevel = nextHintLevel,
            subject = subject,
            detectedMistake = detectMistakeSeeking(enriched),
            encouragesAttempt = !containsDirectAnswer(sanitized)
        )
    }

    private fun resolveSubject(context: TutorContext): StudySubject {
        if (context.subject is StudySubject.Custom) return context.subject
        val builtin = context.subject as? StudySubject.Builtin ?: return StudySubject.Builtin(Subject.GENERAL)
        if (builtin.subject != Subject.GENERAL) return builtin
        if (context.appMode.learningBehavior(context.freeModelVariant) == AppMode.CODE) {
            return StudySubject.Builtin(Subject.GENERAL)
        }
        val classified = subjectClassifier.classify(
            listOfNotNull(context.studentMessage, context.scannedText).joinToString(" ")
        )
        return StudySubject.Builtin(classified)
    }

    private fun determineNextHintLevel(context: TutorContext, response: String): HintLevel {
        val studentAttempted = context.studentMessage.length > 20 &&
            !isAnswerSeeking(context.studentMessage)
        return when {
            studentAttempted && context.hintLevel == HintLevel.NEAR_SOLUTION ->
                HintLevel.STUDENT_ATTEMPT_REQUIRED
            isAnswerSeeking(context.studentMessage) -> context.hintLevel.next()
            else -> context.hintLevel
        }
    }

    private fun isAnswerSeeking(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("what is the answer") ||
            lower.contains("just tell me") ||
            lower.contains("solve this for me") ||
            lower.contains("give me the answer") ||
            lower.contains("write the whole") ||
            lower.contains("build the entire app")
    }

    private fun detectMistakeSeeking(context: TutorContext): String? {
        return when (context.appMode.learningBehavior(context.freeModelVariant)) {
            AppMode.TUTOR -> if (isAnswerSeeking(context.studentMessage)) {
                "It looks like you're asking for a direct answer. Let's work through this step by step instead."
            } else null
            AppMode.CODE -> if (isAnswerSeeking(context.studentMessage)) {
                "I can't build full apps or projects — let's focus on one function or bug at a time."
            } else null
            AppMode.STUDY, AppMode.FREE -> null
        }
    }

    private fun containsDirectAnswer(response: String): Boolean {
        val lower = response.lowercase()
        return lower.contains("the answer is") || lower.contains("the solution is")
    }

    private fun sanitizeResponse(response: String, mode: AppMode): String {
        if (mode != AppMode.TUTOR) return response.trim()
        var sanitized = response
        val forbiddenPatterns = listOf(
            Regex("the answer is\\s+.+", RegexOption.IGNORE_CASE),
            Regex("the solution is\\s+.+", RegexOption.IGNORE_CASE)
        )
        forbiddenPatterns.forEach { pattern ->
            sanitized = sanitized.replace(
                pattern,
                "Let's keep working on this together — what's your next step?"
            )
        }
        return sanitized.trim()
    }
}
