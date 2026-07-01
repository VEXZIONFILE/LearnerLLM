package com.learner.lm.ai

class PromptBuilder {

    fun buildSystemPrompt(context: TutorContext): String {
        val capabilities = SubscriptionCapabilities.forTier(context.subscriptionTier)
        return when (context.appMode) {
            AppMode.TUTOR -> buildTutorSystemPrompt(context, capabilities)
            AppMode.STUDY -> buildStudySystemPrompt(context, capabilities)
            AppMode.CODE -> buildCodeSystemPrompt(context, capabilities)
        }
    }

    fun buildUserPrompt(context: TutorContext): String {
        val capabilities = SubscriptionCapabilities.forTier(context.subscriptionTier)
        val history = context.conversationHistory
            .takeLast(capabilities.conversationHistoryLimit)
            .joinToString("\n") { (role, content) -> "$role: $content" }
        val scanned = context.scannedText?.let { "\n\nScanned material:\n$it" }.orEmpty()
        val subjectLine = subjectLineFor(context.subject)
        val tierNote = when {
            capabilities.isPro -> "Subscription: Premium — maximum depth, longest responses, richest examples."
            capabilities.isPremium -> "Subscription: Pro — provide richer examples and more detail."
            else -> "Subscription: Standard — keep responses concise but correct."
        }

        return when (context.appMode) {
            AppMode.TUTOR -> """
                Grade level: ${context.gradeLevel}
                $subjectLine
                Mode: Tutor (Socratic, step-by-step)
                Hint level: ${context.hintLevel.level}
                $tierNote
                $scanned

                Recent conversation:
                $history

                Student message: ${context.studentMessage}

                Guide the student step-by-step. Ask a question back. Do not give the final answer.
            """.trimIndent()

            AppMode.STUDY -> """
                Grade level: ${context.gradeLevel}
                $subjectLine
                Mode: Study pack generator
                $tierNote
                $scanned

                Topic / request: ${context.studentMessage}

                Produce structured study materials for this topic at grade ${context.gradeLevel} level.
            """.trimIndent()

            AppMode.CODE -> """
                Grade level: ${context.gradeLevel}
                $subjectLine
                Mode: Code help (debug & teach only)
                $tierNote
                $scanned

                Recent conversation:
                $history

                Student code question: ${context.studentMessage}

                Explain and debug in small teachable pieces. Max ~${capabilities.codeMaxSuggestedLines} lines of suggested code.
            """.trimIndent()
        }
    }

    private fun buildTutorSystemPrompt(
        context: TutorContext,
        capabilities: SubscriptionCapabilities
    ): String {
        val gradeGuidance = tutorGradeGuidance(context.gradeLevel)
        val subjectGuidance = subjectGuidanceFor(context.subject)
        val depthGuidance = if (capabilities.isPremium) {
            "Provide up to ${capabilities.tutorExampleCount} worked examples of reasoning (not final answers)."
        } else {
            "Provide at most ${capabilities.tutorExampleCount} brief reasoning example."
        }

        return """
            You are Learner LM Tutor Mode — powered by gpt-oss-120b for grades 6–12.

            ROLE: Primary Socratic tutor. Guide learning step-by-step. Never be a homework solver.

            NON-NEGOTIABLE RULES:
            - NEVER give final answers without prior guided steps
            - NEVER say "the answer is..." or complete the student's work
            - ALWAYS ask questions back to check understanding
            - ALWAYS adapt difficulty to grade ${context.gradeLevel}
            - Break problems into small steps the student can attempt

            HINT LADDER (level ${context.hintLevel.level}):
            - Level 1: Gentle directional nudge
            - Level 2: Deeper concept explanation
            - Level 3: Near-solution guidance (student must finish)
            - Level 4: Require a student attempt before more help

            $gradeGuidance

            $subjectGuidance

            $depthGuidance

            Celebrate reasoning, not just results. When you spot an error, guide discovery — don't correct outright.
        """.trimIndent()
    }

    private fun buildStudySystemPrompt(
        context: TutorContext,
        capabilities: SubscriptionCapabilities
    ): String {
        val sections = when (capabilities.studySections) {
            SubscriptionCapabilities.StudySectionDepth.PRO -> """
            REQUIRED OUTPUT SECTIONS (use these exact headings):
            ## Summary
            ## Key Concepts
            ## Flashcards
            (Format each as Q: ... / A: ...)
            ## Quiz Questions
            (Number each question; include answer key at the end)
            ## Practice Problems
            (3–5 problems with hints — no full solutions)
            """.trimIndent()
            SubscriptionCapabilities.StudySectionDepth.FULL -> """
            REQUIRED OUTPUT SECTIONS (use these exact headings):
            ## Summary
            ## Key Concepts
            ## Flashcards
            (Format each as Q: ... / A: ...)
            ## Quiz Questions
            (Number each question; include answer key at the end)
            """.trimIndent()
            SubscriptionCapabilities.StudySectionDepth.BASIC -> """
            REQUIRED OUTPUT SECTIONS (use these exact headings):
            ## Summary
            ## Key Concepts
            ## Flashcards
            (Format each as Q: ... / A: ... — include at least 3 cards)
            """.trimIndent()
        }

        return """
            You are Learner LM Study Mode — powered by NVIDIA Nemotron 3 Super.
            Behave like a NotebookLM-style study generator for grade ${context.gradeLevel} students.

            ROLE: Convert topics into structured, study-ready materials.

            RULES:
            - Always use clear markdown headings
            - Adapt vocabulary to grade ${context.gradeLevel}
            - Flashcards must be Q/A format
            - Quiz questions must test understanding, not trivia
            - Never do homework for the student — create materials to learn FROM

            $sections
        """.trimIndent()
    }

    private fun buildCodeSystemPrompt(
        context: TutorContext,
        capabilities: SubscriptionCapabilities
    ): String {
        val lineLimit = capabilities.codeMaxSuggestedLines
        val depth = when {
            capabilities.isPro ->
                "Give exhaustive line-by-line explanations, multiple debugging strategies, and edge-case notes."
            capabilities.isPremium ->
                "Give detailed line-by-line explanations and multiple debugging strategies."
            else -> "Give concise explanations focused on the immediate bug or concept."
        }

        return """
            You are Learner LM Code Help Mode — powered by Poolside Laguna M.1.
            Programming tutor for grade ${context.gradeLevel} students learning to code.

            ROLE: Debug and explain code step-by-step. Teach programming — do not build projects for students.

            NON-NEGOTIABLE RULES:
            - NEVER generate full applications, full systems, or complete projects
            - ONLY help with: single functions, small components, bug fixes, syntax, logic errors
            - Maximum ~$lineLimit lines of suggested code per response
            - Explain WHY each fix works
            - If asked to build an entire app, refuse politely and offer to break it into small learning steps

            $depth

            Use code blocks with language tags. Ask the student what they expect the code to do before fixing.
        """.trimIndent()
    }

    private fun tutorGradeGuidance(grade: Int): String = when (grade) {
        in 6..8 -> """
            Grades 6–8: Use simple language, short sentences, heavy step-by-step guidance.
            Define every new term. Check understanding after each step.
        """.trimIndent()
        in 9..10 -> """
            Grades 9–10: Moderate explanation depth. Introduce technical terms with brief definitions.
            Balance guidance with student independence.
        """.trimIndent()
        in 11..12 -> """
            Grades 11–12: Faster reasoning, less hand-holding. Use precise academic language.
            Expect more student initiative; probe with harder questions.
        """.trimIndent()
        else -> "Adapt explanations to an appropriate middle or high school level."
    }

    private fun subjectLineFor(subject: StudySubject): String = when (subject) {
        is StudySubject.Builtin -> "Subject: ${subject.displayName}"
        is StudySubject.Custom -> "Subject: ${subject.displayName} (${subject.category.label})"
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
            Custom subject: "${subject.displayName}" (${subject.category.label}).
            Tailor tutoring to this context while maintaining Socratic guidance.
        """.trimIndent()
    }
}
