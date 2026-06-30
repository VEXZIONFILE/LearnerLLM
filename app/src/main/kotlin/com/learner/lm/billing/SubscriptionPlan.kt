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
            description = "Deeper AI tutoring, richer study packs, and better code help.",
            features = listOf(
                "Unlimited homework scans",
                "Deeper AI tutoring with more examples",
                "Full study packs with quizzes & flashcards",
                "Advanced code debugging walkthroughs",
                "Unlimited chat across all 3 models"
            ),
            isPopular = true
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.PREMIUM_YEARLY,
            tier = SubscriptionTier.BASIC,
            title = "Premium",
            price = "$99.90",
            period = "/ year",
            description = "Best value — pay for 10 months, get 12 ($9.99 × 10).",
            features = listOf(
                "Everything in Premium monthly",
                "Unlimited homework scans",
                "Save $19.98 vs paying monthly",
                "Best value for daily learners"
            ),
            badge = "2 months free"
        )
    )
}
