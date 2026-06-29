package com.learner.lm.utils

import java.util.UUID

object SessionUtils {
    fun createSessionId(): String = UUID.randomUUID().toString()
}

object GradeLevelValidator {
    fun clamp(grade: Int): Int = grade.coerceIn(6, 12)
}
