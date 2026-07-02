package com.learner.lm.ai

/**
 * OpenRouter free-tier models. Each variant can be used in any [AppMode].
 */
enum class FreeModelVariant(
    val label: String,
    val shortLabel: String,
    val modelId: String,
    val displayName: String,
    val quotaLabel: String
) {
    TUTOR(
        label = "GPT-OSS",
        shortLabel = "GPT-OSS",
        modelId = "openai/gpt-oss-120b:free",
        displayName = "GPT-OSS",
        quotaLabel = "GPT-OSS"
    ),
    STUDY(
        label = "Nemotron",
        shortLabel = "Nemotron",
        modelId = "nvidia/nemotron-3-super-120b-a12b:free",
        displayName = "Nemotron",
        quotaLabel = "Nemotron"
    ),
    CODE(
        label = "Laguna",
        shortLabel = "Laguna",
        modelId = "poolside/laguna-m.1:free",
        displayName = "Laguna",
        quotaLabel = "Laguna"
    );

    companion object {
        fun defaultForMode(mode: AppMode): FreeModelVariant = when (mode) {
            AppMode.STUDY -> STUDY
            AppMode.CODE -> CODE
            else -> TUTOR
        }
    }
}

fun AppMode.learningBehavior(@Suppress("UNUSED_PARAMETER") freeModelVariant: FreeModelVariant): AppMode =
    when (this) {
        AppMode.FREE -> AppMode.TUTOR
        else -> this
    }
