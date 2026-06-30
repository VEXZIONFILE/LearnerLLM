package com.learner.lm.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.learner.lm.billing.ScanQuotaExceededException
import com.learner.lm.billing.ScanQuotaPolicy
import com.learner.lm.billing.ScanQuotaStatus
import com.learner.lm.billing.SubscriptionTier
import java.time.LocalDate
import kotlinx.coroutines.tasks.await

/**
 * Tracks homework scan usage in Firebase Firestore per authenticated user.
 * Survives sign-out/sign-in and app reinstalls (tied to Firebase Auth UID).
 *
 * Path: users/{uid}/scan_usage/{yyyy-MM-dd}
 */
class ScanQuotaRepository(context: Context) {

    private val appContext = context.applicationContext

    private val firebaseAvailable: Boolean by lazy {
        try {
            FirebaseApp.getApps(appContext).isNotEmpty() || FirebaseApp.initializeApp(appContext) != null
        } catch (_: Exception) {
            false
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (firebaseAvailable) FirebaseFirestore.getInstance() else null
    }

    fun isPremiumTier(tierName: String): Boolean =
        tierName == SubscriptionTier.BASIC.name || tierName == SubscriptionTier.PRO.name

    suspend fun fetchStatus(userId: String, tierName: String): Result<ScanQuotaStatus> {
        if (userId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to scan homework."))
        }
        if (isPremiumTier(tierName)) {
            return Result.success(ScanQuotaStatus.forTier(usedToday = 0, tierName))
        }

        return try {
            val usedToday = readTodayCount(userId)
            Result.success(ScanQuotaStatus.forTier(usedToday, tierName))
        } catch (error: Exception) {
            Result.failure(
                IllegalStateException(
                    "Could not load scan quota. Check your connection and try again.",
                    error
                )
            )
        }
    }

    /**
     * Atomically records a successful scan on the server after OCR succeeds.
     * Uses a Firestore transaction so concurrent captures cannot exceed the limit.
     */
    suspend fun recordSuccessfulScan(userId: String, tierName: String): Result<ScanQuotaStatus> {
        if (userId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in required to scan homework."))
        }
        if (isPremiumTier(tierName)) {
            return Result.success(ScanQuotaStatus.forTier(usedToday = 0, tierName))
        }

        val db = firestore
            ?: return Result.failure(IllegalStateException("Firebase not configured. Add google-services.json."))

        return try {
            val today = todayYmd()
            val docRef = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SCAN_USAGE)
                .document(today)

            val newCount = db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val current = snapshot.getLong(FIELD_COUNT)?.toInt() ?: 0
                if (current >= ScanQuotaPolicy.FREE_DAILY_LIMIT) {
                    throw ScanQuotaExceededException()
                }
                val updated = current + 1
                transaction.set(
                    docRef,
                    mapOf(
                        FIELD_COUNT to updated,
                        FIELD_DATE to today,
                        FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                updated
            }.await()

            Result.success(ScanQuotaStatus.forTier(newCount, tierName))
        } catch (error: ScanQuotaExceededException) {
            Result.failure(error)
        } catch (error: Exception) {
            Result.failure(
                IllegalStateException(
                    "Could not save scan usage. Check your connection and try again.",
                    error
                )
            )
        }
    }

    private suspend fun readTodayCount(userId: String): Int {
        val db = firestore
            ?: throw IllegalStateException("Firebase not configured. Add google-services.json.")

        val snapshot = db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_SCAN_USAGE)
            .document(todayYmd())
            .get()
            .await()

        return snapshot.getLong(FIELD_COUNT)?.toInt() ?: 0
    }

    private fun todayYmd(): String = LocalDate.now().toString()

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SCAN_USAGE = "scan_usage"
        private const val FIELD_COUNT = "count"
        private const val FIELD_DATE = "date"
        private const val FIELD_UPDATED_AT = "updatedAt"
    }
}
