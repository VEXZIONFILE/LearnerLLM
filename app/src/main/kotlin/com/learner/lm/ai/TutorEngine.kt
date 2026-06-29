package com.learner.lm.ai

import com.learner.lm.repository.AiRepository

class TutorEngine(
    private val aiRepository: AiRepository,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val subjectClassifier: SubjectClassifier = SubjectClassifier()
) {

    suspend fun respond(context: TutorContext): TutorResponse {
        val subject = if (context.subject == Subject.GENERAL) {
            subjectClassifier.classify(
                listOfNotNull(context.studentMessage, context.scannedText).joinToString(" ")
            )
        } else {
            context.subject
        }

        val enrichedContext = context.copy(subject = subject)
        val systemPrompt = promptBuilder.buildSystemPrompt(enrichedContext.gradeLevel, subject)
        val userPrompt = promptBuilder.buildUserPrompt(enrichedContext)

        val message = aiRepository.generateTutorResponse(systemPrompt, userPrompt)
        val sanitized = sanitizeResponse(message)
        val nextHintLevel = determineNextHintLevel(enrichedContext, sanitized)

        return TutorResponse(
            message = sanitized,
            hintLevel = nextHintLevel,
            subject = subject,
            detectedMistake = detectMistakeSeeking(enrichedContext.studentMessage),
            encouragesAttempt = !containsDirectAnswer(sanitized)
        )
    }

    private fun determineNextHintLevel(context: TutorContext, response: String): HintLevel {
        val studentAttempted = context.studentMessage.length > 20 &&
            !isAnswerSeeking(context.studentMessage)
        return when {
            studentAttempted && context.hintLevel == HintLevel.NEAR_SOLUTION -> HintLevel.STUDENT_ATTEMPT_REQUIRED
            isAnswerSeeking(context.studentMessage) -> context.hintLevel.next()
            else -> context.hintLevel
        }
    }

    private fun isAnswerSeeking(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("what is the answer") ||
            lower.contains("just tell me") ||
            lower.contains("solve this for me") ||
            lower.contains("give me the answer")
    }

    private fun detectMistakeSeeking(message: String): String? {
        return if (isAnswerSeeking(message)) {
            "It looks like you're asking for a direct answer. Let's work through this step by step instead."
        } else {
            null
        }
    }

    private fun containsDirectAnswer(response: String): Boolean {
        val lower = response.lowercase()
        return lower.contains("the answer is") || lower.contains("the solution is")
    }

    private fun sanitizeResponse(response: String): String {
        var sanitized = response
        val forbiddenPatterns = listOf(
            Regex("the answer is\\s+.+", RegexOption.IGNORE_CASE),
            Regex("the solution is\\s+.+", RegexOption.IGNORE_CASE)
        )
        forbiddenPatterns.forEach { pattern ->
            sanitized = sanitized.replace(pattern, "Let's keep working on this together — what's your next step?")
        }
        return sanitized.trim()
    }
}
