package com.learner.lm.ai

import com.learner.lm.BuildConfig

object AiConfig {
    const val TUTOR_MODEL_ID = ModelRegistry.TUTOR_MODEL
    const val STUDY_MODEL_ID = ModelRegistry.STUDY_MODEL
    const val CODE_MODEL_ID = ModelRegistry.CODE_MODEL

    /** Default display name for branding */
    const val MODEL_DISPLAY_NAME = "LearnerLM"

    val apiBaseUrl: String = BuildConfig.OPENROUTER_BASE_URL
    val apiKey: String = BuildConfig.OPENROUTER_API_KEY
    val appReferer: String = BuildConfig.APP_REFERER
}
