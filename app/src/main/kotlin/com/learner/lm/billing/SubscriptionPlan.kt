package com.learner.lm.billing

object SubscriptionProducts {
    /** Monthly Pro — maps to BASIC tier */
    const val PRO_MONTHLY = "learnerlm_basic_monthly"
    /** Monthly Premium — maps to PRO tier */
    const val PREMIUM_MONTHLY = "learnerlm_pro_monthly"
    /** Annual Mega — maps to BASIC tier */
    const val MEGA_YEARLY = "learnerlm_pro_yearly"

    val allProductIds = listOf(PRO_MONTHLY, PREMIUM_MONTHLY, MEGA_YEARLY)
}

enum class SubscriptionTier(val displayName: String) {
    FREE("Standard"),
    BASIC("Pro"),
    PRO("Premium")
}

data class SubscriptionPlan(
    val productId: String,
    val tier: SubscriptionTier,
    val title: String,
    val price: String,
    val period: String,
    val description: String,
    val features: List<String>,
    val badge: String? = null,
    val isPopular: Boolean = false
)

object SubscriptionCatalog {
    val plans = listOf(
        SubscriptionPlan(
            productId = SubscriptionProducts.PRO_MONTHLY,
            tier = SubscriptionTier.BASIC,
            title = "Pro",
            price = "$9.99",
            period = "/ month",
            description = "Unlimited scans, deeper AI, and full study packs.",
            features = listOf(
                "Unlimited homework scans",
                "2× longer AI responses",
                "Full study packs with quizzes & flashcards",
                "Advanced code debugging (40 lines)",
                "Up to 3 tutor examples per reply",
                "12-message chat memory"
            ),
            isPopular = true
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.PREMIUM_MONTHLY,
            tier = SubscriptionTier.PRO,
            title = "Premium",
            price = "$14.99",
            period = "/ month",
            description = "Maximum AI depth for power learners.",
            features = listOf(
                "Everything in Pro",
                "Longest responses — 3× Standard depth",
                "Practice problem sets in Study mode",
                "Up to 5 tutor examples per reply",
                "16-message chat memory",
                "60-line code walkthroughs"
            ),
            badge = "Best for daily use"
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.MEGA_YEARLY,
            tier = SubscriptionTier.BASIC,
            title = "Mega",
            price = "$190.00",
            period = "/ year",
            description = "Best value — pay for 10 months, get 12 (2 months free).",
            features = listOf(
                "Everything in Pro monthly",
                "Unlimited homework scans",
                "2 months free vs paying monthly",
                "Best value for committed learners"
            ),
            badge = "2 months free"
        )
    )
}
