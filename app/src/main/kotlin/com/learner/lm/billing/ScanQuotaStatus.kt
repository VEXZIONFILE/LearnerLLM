package com.learner.lm.billing

data class ScanQuotaStatus(
    val usedToday: Int,
    val isPremium: Boolean,
    val canScan: Boolean,
    val remainingScans: Int?,
    val quotaLabel: String
) {
    companion object {
        fun forTier(usedToday: Int, tierName: String): ScanQuotaStatus {
            val premium = tierName == SubscriptionTier.BASIC.name ||
                tierName == SubscriptionTier.PRO.name
            val canScan = ScanQuotaPolicy.canScan(usedToday, premium)
            return ScanQuotaStatus(
                usedToday = usedToday,
                isPremium = premium,
                canScan = canScan,
                remainingScans = ScanQuotaPolicy.remainingScans(usedToday, premium),
                quotaLabel = ScanQuotaPolicy.quotaLabel(usedToday, premium)
            )
        }
    }
}

class ScanQuotaExceededException :
    IllegalStateException("Daily homework scan limit reached")
