package com.learner.lm.repository

import com.learner.lm.BuildConfig

object LearnerApiConfig {
    val baseUrl: String = BuildConfig.LEARNER_API_BASE_URL
    val isConfigured: Boolean get() = baseUrl.isNotBlank()
}
