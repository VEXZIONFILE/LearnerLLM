package com.learner.lm.billing

data class PlanComparisonRow(
    val feature: String,
    val standard: String,
    val premium: String,
    val premiumHighlight: Boolean = false
)

object SubscriptionFeatures {
    val comparisonRows = listOf(
        PlanComparisonRow(
            feature = "Homework scans",
            standard = "3 per day",
            premium = "Unlimited",
            premiumHighlight = true
        ),
        PlanComparisonRow(
            feature = "AI models",
            standard = "Learner Tutor, Study & Code",
            premium = "All models + deeper responses"
        ),
        PlanComparisonRow(
            feature = "Study packs",
            standard = "Summary + basics",
            premium = "Full packs with quizzes",
            premiumHighlight = true
        ),
        PlanComparisonRow(
            feature = "Code help depth",
            standard = "Short explanations",
            premium = "Longer debugging walkthroughs"
        ),
        PlanComparisonRow(
            feature = "Tutor examples",
            standard = "1 example per reply",
            premium = "Up to 3 examples per reply"
        ),
        PlanComparisonRow(
            feature = "Chat",
            standard = "Unlimited",
            premium = "Unlimited"
        )
    )

    val premiumReasons = listOf(
        "Scan every worksheet without daily limits",
        "Get richer study packs with flashcards and quizzes",
        "Unlock deeper tutoring with more examples",
        "Receive better step-by-step code debugging"
    )
}
