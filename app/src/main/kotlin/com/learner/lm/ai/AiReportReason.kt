package com.learner.lm.ai

enum class AiReportReason(val apiValue: String, val label: String) {
    OFFENSIVE("OFFENSIVE", "Offensive or inappropriate"),
    HARMFUL("HARMFUL", "Harmful or dangerous"),
    INACCURATE("INACCURATE", "Inaccurate or misleading"),
    SPAM("SPAM", "Spam or irrelevant"),
    OTHER("OTHER", "Other");

    companion object {
        fun fromApiValue(value: String): AiReportReason? =
            entries.firstOrNull { it.apiValue == value }
    }
}
