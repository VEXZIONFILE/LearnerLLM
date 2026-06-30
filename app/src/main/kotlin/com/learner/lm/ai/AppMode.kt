package com.learner.lm.ai

/**
 * Primary learning modes — each routes to a dedicated OpenRouter model.
 */
enum class AppMode(
    val label: String,
    val shortLabel: String,
    val description: String
) {
    TUTOR(
        label = "Tutor",
        shortLabel = "Tutor",
        description = "Step-by-step Socratic tutoring for grades 6–12"
    ),
    STUDY(
        label = "Study",
        shortLabel = "Study",
        description = "NotebookLM-style summaries, flashcards, and quizzes"
    ),
    CODE(
        label = "Code Help",
        shortLabel = "Code",
        description = "Debug and explain code in small, teachable pieces"
    )
}
