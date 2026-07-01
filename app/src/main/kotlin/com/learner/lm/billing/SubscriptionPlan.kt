package com.learner.lm.billing

object SubscriptionProducts {
    const val PREMIUM_MONTHLY = "learnerlm_basic_monthly"
    const val PREMIUM_YEARLY = "learnerlm_pro_yearly"
    const val PRO_MONTHLY = "learnerlm_pro_monthly"

    val allProductIds = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY, PRO_MONTHLY)
}

enum class SubscriptionTier(val displayName: String) {
    FREE("Standard"),
    BASIC("Premium"),
    PRO("Premium Pro")
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
            productId = SubscriptionProducts.PREMIUM_MONTHLY,
            tier = SubscriptionTier.BASIC,
            title = "Premium",
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
            productId = SubscriptionProducts.PRO_MONTHLY,
            tier = SubscriptionTier.PRO,
            title = "Premium Pro",
            price = "$14.99",
            period = "/ month",
            description = "Maximum AI depth for power learners.",
            features = listOf(
                "Everything in Premium",
                "Longest responses — 3× Standard depth",
                "Practice problem sets in Study mode",
                "Up to 5 tutor examples per reply",
                "16-message chat memory",
                "60-line code walkthroughs"
            ),
            badge = "Best for daily use"
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.PREMIUM_YEARLY,
            tier = SubscriptionTier.BASIC,
            title = "Premium Annual",
            price = "$99.90",
            period = "/ year",
            description = "Best value — pay for 10 months, get 12.",
            features = listOf(
                "Everything in Premium monthly",
                "Unlimited homework scans",
                "Save $19.98 vs paying monthly",
                "Best value for committed learners"
            ),
            badge = "2 months free"
        )
    )
}
