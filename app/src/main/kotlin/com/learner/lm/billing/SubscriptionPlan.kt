package com.learner.lm.billing

object SubscriptionProducts {
    const val BASIC_MONTHLY = "learnerlm_basic_monthly"
    const val PRO_MONTHLY = "learnerlm_pro_monthly"
    const val PRO_YEARLY = "learnerlm_pro_yearly"

    val allProductIds = listOf(BASIC_MONTHLY, PRO_MONTHLY, PRO_YEARLY)
}

enum class SubscriptionTier(val displayName: String) {
    FREE("Free"),
    BASIC("Learner Basic"),
    PRO("Learner Pro")
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
            productId = SubscriptionProducts.BASIC_MONTHLY,
            tier = SubscriptionTier.BASIC,
            title = "Learner Basic",
            price = "$9.99",
            period = "/ month",
            description = "Essential Socratic tutoring for everyday homework help.",
            features = listOf(
                "Unlimited AI study chat",
                "Homework scanner",
                "Custom subjects",
                "Progress tracking"
            )
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.PRO_MONTHLY,
            tier = SubscriptionTier.PRO,
            title = "Learner Pro",
            price = "$19.99",
            period = "/ month",
            description = "Advanced tutoring with priority AI and deeper learning insights.",
            features = listOf(
                "Everything in Basic",
                "Priority LearnerLM model access",
                "Weakness detection",
                "Practice problem generator",
                "Voice tutor mode"
            ),
            isPopular = true
        ),
        SubscriptionPlan(
            productId = SubscriptionProducts.PRO_YEARLY,
            tier = SubscriptionTier.PRO,
            title = "Learner Pro",
            price = "$199.90",
            period = "/ year",
            description = "Best value — 10 months of Pro ($19.99 × 10), get 12.",
            features = listOf(
                "Everything in Learner Pro",
                "2 months free ($39.98 savings)",
                "vs $239.88/year at monthly rate"
            ),
            badge = "2 months free"
        )
    )
}
