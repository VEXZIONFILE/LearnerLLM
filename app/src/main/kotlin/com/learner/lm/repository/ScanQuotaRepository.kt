package com.learner.lm.repository

import android.content.Context
import com.learner.lm.billing.ScanQuotaPolicy
import com.learner.lm.billing.SubscriptionTier
import java.time.LocalDate

class ScanQuotaRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPremiumTier(tierName: String): Boolean =
        tierName == SubscriptionTier.BASIC.name || tierName == SubscriptionTier.PRO.name

    fun getTodayScanCount(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_COUNT, 0)
    }

    fun canScan(tierName: String): Boolean =
        ScanQuotaPolicy.canScan(getTodayScanCount(), isPremiumTier(tierName))

    fun remainingScans(tierName: String): Int? =
        ScanQuotaPolicy.remainingScans(getTodayScanCount(), isPremiumTier(tierName))

    fun quotaLabel(tierName: String): String =
        ScanQuotaPolicy.quotaLabel(getTodayScanCount(), isPremiumTier(tierName))

    fun recordScan(tierName: String) {
        if (isPremiumTier(tierName)) return
        resetIfNewDay()
        val updated = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit()
            .putString(KEY_DATE, todayYmd())
            .putInt(KEY_COUNT, updated)
            .apply()
    }

    private fun resetIfNewDay() {
        val today = todayYmd()
        val savedDate = prefs.getString(KEY_DATE, null)
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putInt(KEY_COUNT, 0)
                .apply()
        }
    }

    private fun todayYmd(): String = LocalDate.now().toString()

    companion object {
        private const val PREFS_NAME = "homework_scan_quota"
        private const val KEY_DATE = "date_ymd"
        private const val KEY_COUNT = "scan_count"
    }
}
