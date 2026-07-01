package com.learner.lm.ai

import com.learner.lm.repository.LearnerApiConfig

object AiConfig {
    const val TUTOR_MODEL_ID = ModelRegistry.TUTOR_MODEL
    const val STUDY_MODEL_ID = ModelRegistry.STUDY_MODEL
    const val CODE_MODEL_ID = ModelRegistry.CODE_MODEL
    const val FREE_TUTOR_MODEL_ID = ModelRegistry.FREE_TUTOR_MODEL
    const val FREE_STUDY_MODEL_ID = ModelRegistry.FREE_STUDY_MODEL
    const val FREE_CODE_MODEL_ID = ModelRegistry.FREE_CODE_MODEL

    /** Default display name for branding */
    const val MODEL_DISPLAY_NAME = "LearnerLM"

    val apiBaseUrl: String = LearnerApiConfig.baseUrl
    val isBackendConfigured: Boolean = LearnerApiConfig.isConfigured
}
