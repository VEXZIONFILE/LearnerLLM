package com.learner.lm.ai

enum class SubjectCategory(val label: String, val emoji: String) {
    CLASS("Class", "🏫"),
    AFTER_SCHOOL("After School", "🎨"),
    PROJECT("Project", "📋"),
    CLUB("Club / Activity", "⚽"),
    EXAM_PREP("Exam Prep", "📝"),
    OTHER("Other", "✨")
}

enum class Subject(val displayLabel: String) {
    MATH("Math"),
    SCIENCE("Science"),
    ENGLISH("English"),
    HISTORY("History"),
    GEOGRAPHY("Geography"),
    GENERAL("General")
}

sealed class StudySubject {
    abstract val displayName: String
    abstract val storageKey: String
    abstract val categoryLabel: String?

    data class Builtin(val subject: Subject) : StudySubject() {
        override val displayName: String = subject.displayLabel
        override val storageKey: String = "builtin:${subject.name}"
        override val categoryLabel: String? = null
    }

    data class Custom(
        val id: Long,
        val name: String,
        val category: SubjectCategory,
        val emoji: String = category.emoji
    ) : StudySubject() {
        override val displayName: String = name
        override val storageKey: String = "custom:$id"
        override val categoryLabel: String = category.label
    }

    companion object {
        val builtinSubjects: List<Builtin> = Subject.entries.map { Builtin(it) }

        fun fromStorageKey(key: String, customSubjects: List<Custom> = emptyList()): StudySubject {
            if (key.startsWith("custom:")) {
                val id = key.removePrefix("custom:").toLongOrNull()
                return customSubjects.find { it.id == id }
                    ?: Builtin(Subject.GENERAL)
            }
            val name = key.removePrefix("builtin:")
            val subject = Subject.entries.find { it.name == name } ?: Subject.GENERAL
            return Builtin(subject)
        }
    }
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
    val subject: StudySubject,
    val appMode: AppMode = AppMode.TUTOR,
    val subscriptionTier: String = "FREE",
    val hintLevel: HintLevel = HintLevel.GENTLE_NUDGE,
    val studentMessage: String,
    val conversationHistory: List<Pair<String, String>> = emptyList(),
    val scannedText: String? = null
)

data class TutorResponse(
    val message: String,
    val hintLevel: HintLevel,
    val subject: StudySubject,
    val detectedMistake: String? = null,
    val encouragesAttempt: Boolean = true
)
