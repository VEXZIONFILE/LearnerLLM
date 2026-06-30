package com.learner.lm.billing

object ScanQuotaPolicy {
    const val FREE_DAILY_LIMIT = 3

    fun canScan(usedToday: Int, isPremium: Boolean): Boolean =
        isPremium || usedToday < FREE_DAILY_LIMIT

    fun remainingScans(usedToday: Int, isPremium: Boolean): Int? =
        if (isPremium) null else (FREE_DAILY_LIMIT - usedToday).coerceAtLeast(0)

    fun quotaLabel(usedToday: Int, isPremium: Boolean): String =
        if (isPremium) "Unlimited scans" else "${remainingScans(usedToday, isPremium)} of $FREE_DAILY_LIMIT scans left today"
}
