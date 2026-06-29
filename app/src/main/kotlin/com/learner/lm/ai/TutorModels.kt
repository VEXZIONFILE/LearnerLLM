package com.learner.lm.ai

enum class Subject {
    MATH,
    SCIENCE,
    ENGLISH,
    HISTORY,
    GEOGRAPHY,
    GENERAL
}

enum class HintLevel(val level: Int) {
    GENTLE_NUDGE(1),
    DEEPER_EXPLANATION(2),
    NEAR_SOLUTION(3),
    STUDENT_ATTEMPT_REQUIRED(4);

    fun next(): HintLevel = entries.getOrElse(level) { STUDENT_ATTEMPT_REQUIRED }
}

data class TutorContext(
    val gradeLevel: Int,
    val subject: Subject,
    val hintLevel: HintLevel = HintLevel.GENTLE_NUDGE,
    val studentMessage: String,
    val conversationHistory: List<Pair<String, String>> = emptyList(),
    val scannedText: String? = null
)

data class TutorResponse(
    val message: String,
    val hintLevel: HintLevel,
    val subject: Subject,
    val detectedMistake: String? = null,
    val encouragesAttempt: Boolean = true
)
