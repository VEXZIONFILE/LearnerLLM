package com.learner.lm.ai

import com.learner.lm.BuildConfig

object AiConfig {
    /** OpenRouter model slug for OpenAI gpt-oss-120b */
    const val MODEL_ID = "openai/gpt-oss-120b"

    /** User-facing model name shown in the app */
    const val MODEL_DISPLAY_NAME = "LearnerLM"

    val apiBaseUrl: String = BuildConfig.OPENROUTER_BASE_URL
    val apiKey: String = BuildConfig.OPENROUTER_API_KEY
    val appReferer: String = BuildConfig.APP_REFERER
}
