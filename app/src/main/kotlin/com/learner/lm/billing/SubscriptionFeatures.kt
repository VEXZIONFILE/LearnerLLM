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
            standard = "Tutor, Study & Code",
            premium = "Frontier models + deeper reasoning"
        ),
        PlanComparisonRow(
            feature = "Response length",
            standard = "Short & focused",
            premium = "2–3× longer answers",
            premiumHighlight = true
        ),
        PlanComparisonRow(
            feature = "Study packs",
            standard = "Summary + flashcards",
            premium = "Quizzes + practice problems (Pro)"
        ),
        PlanComparisonRow(
            feature = "Tutor examples",
            standard = "1 per reply",
            premium = "Up to 5 per reply (Pro)"
        ),
        PlanComparisonRow(
            feature = "Code debugging",
            standard = "~20 lines of help",
            premium = "Up to 60 lines + strategies (Pro)"
        ),
        PlanComparisonRow(
            feature = "Chat memory",
            standard = "Last 8 messages",
            premium = "Last 16 messages (Pro)"
        ),
        PlanComparisonRow(
            feature = "Quick actions",
            standard = "Basic suggestions",
            premium = "Explain simpler · Quiz me · Examples"
        ),
        PlanComparisonRow(
            feature = "Priority depth",
            standard = "—",
            premium = "Premium Pro only",
            premiumHighlight = true
        )
    )

    val premiumReasons = listOf(
        "Unlimited homework scans every day",
        "Longer, smarter answers from frontier AI models",
        "Full study packs with quizzes and flashcards",
        "Premium Pro adds practice problems and max-depth tutoring",
        "Quick-action buttons: explain simpler, quiz me, give examples"
    )

    val proExclusiveBenefits = listOf(
        "Longest AI responses across Tutor, Study & Code",
        "Practice problem sets in Study mode",
        "Up to 5 worked examples per tutor reply",
        "16-message conversation memory",
        "60-line code debugging walkthroughs"
    )
}
