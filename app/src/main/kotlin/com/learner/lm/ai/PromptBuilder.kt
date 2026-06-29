package com.learner.lm.ai

class PromptBuilder {

    fun buildSystemPrompt(gradeLevel: Int, subject: StudySubject): String {
        val gradeGuidance = gradeGuidanceFor(gradeLevel)
        val subjectGuidance = subjectGuidanceFor(subject)

        return """
            You are Learner LM, an AI-powered learning companion for students in grades 6-12.
            You are powered by the LearnerLM model.

            CORE PHILOSOPHY: You are NOT a homework solver. You are a thinking partner that helps
            students learn how to solve problems, not just get answers.

            CRITICAL RULES (NON-NEGOTIABLE):
            - NEVER provide final answers to homework problems
            - NEVER say "the answer is..." or give completed solutions
            - NEVER write full essays or solve math problems directly
            - ALWAYS use the Socratic method: hints, guiding questions, and step-by-step thinking
            - ALWAYS encourage the student to attempt the problem themselves
            - Adapt language and complexity to grade $gradeLevel

            HINT LADDER SYSTEM:
            - Hint 1: Gentle directional nudge
            - Hint 2: Deeper explanation of the concept
            - Hint 3: Near-solution guidance (incomplete — student must finish)
            - Final: Require a student attempt before revealing more

            $gradeGuidance

            $subjectGuidance

            When a student shares a correct attempt, celebrate their reasoning process, not just the result.
            When you detect a reasoning error, guide them to discover and fix it themselves.
        """.trimIndent()
    }

    fun buildUserPrompt(context: TutorContext): String {
        val history = context.conversationHistory.takeLast(6).joinToString("\n") { (role, content) ->
            "$role: $content"
        }
        val scanned = context.scannedText?.let { "\n\nScanned homework text:\n$it" }.orEmpty()
        val subjectLine = when (val subject = context.subject) {
            is StudySubject.Builtin -> "Subject: ${subject.displayName}"
            is StudySubject.Custom -> "Subject: ${subject.displayName} (${subject.category.label})"
        }

        return """
            Grade level: ${context.gradeLevel}
            $subjectLine
            Current hint level: ${context.hintLevel.level} (${context.hintLevel.name})
            $scanned

            Recent conversation:
            $history

            Student message: ${context.studentMessage}

            Respond as a Socratic tutor at hint level ${context.hintLevel.level}.
            Do not give the final answer.
        """.trimIndent()
    }

    private fun gradeGuidanceFor(grade: Int): String = when (grade) {
        in 6..7 -> "Use simple vocabulary, concrete examples, and short sentences."
        in 8..9 -> "Use clear explanations with some technical terms defined inline."
        in 10..12 -> "Use precise academic language while still guiding rather than solving."
        else -> "Adapt explanations to an appropriate middle or high school level."
    }

    private fun subjectGuidanceFor(subject: StudySubject): String = when (subject) {
        is StudySubject.Builtin -> when (subject.subject) {
            Subject.MATH -> "Focus on reasoning and formula discovery. Never compute final answers."
            Subject.SCIENCE -> "Explain processes, cause-effect relationships, and use analogies."
            Subject.ENGLISH -> "Give grammar and structure hints. Never write complete essays."
            Subject.HISTORY -> "Guide timeline reasoning and contextual analysis."
            Subject.GEOGRAPHY -> "Encourage spatial reasoning and location analysis."
            Subject.GENERAL -> "Break problems into smaller parts and ask guiding questions."
        }
        is StudySubject.Custom -> """
            The student created a custom subject: "${subject.displayName}" (${subject.category.label}).
            Tailor your tutoring to this context — e.g. after-school projects need planning help,
            club activities need collaborative thinking, and class subjects need concept reinforcement.
            Never do the work for them; guide their thinking with questions and hints.
        """.trimIndent()
    }
}
