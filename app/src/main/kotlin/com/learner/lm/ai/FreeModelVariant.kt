package com.learner.lm.ai

/**
 * OpenRouter free-tier models used inside [AppMode.FREE].
 */
enum class FreeModelVariant(
    val label: String,
    val shortLabel: String,
    val modelId: String,
    val displayName: String
) {
    TUTOR(
        label = "Free Tutor",
        shortLabel = "Tutor",
        modelId = "openai/gpt-oss-120b:free",
        displayName = "Learner Free Tutor"
    ),
    STUDY(
        label = "Free Study",
        shortLabel = "Study",
        modelId = "nvidia/nemotron-3-super-120b-a12b:free",
        displayName = "Learner Free Study"
    ),
    CODE(
        label = "Free Code",
        shortLabel = "Code",
        modelId = "poolside/laguna-m.1:free",
        displayName = "Learner Free Code"
    );

    val learningStyle: AppMode
        get() = when (this) {
            TUTOR -> AppMode.TUTOR
            STUDY -> AppMode.STUDY
            CODE -> AppMode.CODE
        }
}

fun AppMode.learningBehavior(freeModelVariant: FreeModelVariant): AppMode =
    if (this == AppMode.FREE) freeModelVariant.learningStyle else this
