package com.learner.lm.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.learner.lm.BuildConfig
import com.learner.lm.database.UserProfileDao
import com.learner.lm.database.UserProfileEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val context: Context,
    private val userProfileDao: UserProfileDao
) {
    private val firebaseAvailable: Boolean by lazy {
        try {
            FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
        } catch (_: Exception) {
            false
        }
    }

    private val auth: FirebaseAuth? by lazy {
        if (firebaseAvailable) FirebaseAuth.getInstance() else null
    }

    val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(AuthState.SignedOut)
            } else {
                val profile = UserProfile(
                    uid = user.uid,
                    displayName = user.displayName.orEmpty().ifBlank { "Student" },
                    email = user.email.orEmpty(),
                    photoUrl = user.photoUrl?.toString()
                )
                trySend(AuthState.SignedIn(profile))
            }
        }
        if (auth != null) {
            auth?.addAuthStateListener(listener)
            awaitClose { auth?.removeAuthStateListener(listener) }
        } else {
            trySend(AuthState.SignedOut)
            awaitClose { }
        }
    }

    fun observeLocalProfile(uid: String): Flow<UserProfile?> =
        userProfileDao.observeProfile(uid).map { entity ->
            entity?.toUserProfile()
        }

    suspend fun getGoogleSignInIntent(activity: Activity): android.content.Intent = run {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        require(webClientId.isNotBlank()) {
            "Add GOOGLE_WEB_CLIENT_ID to local.properties. See google-services.json.example."
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(activity, options).signInIntent
    }

    suspend fun handleGoogleSignInResult(data: android.content.Intent?): Result<UserProfile> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
                ?: return Result.failure(IllegalStateException("Google sign-in failed: no ID token"))

            val firebaseAuth = auth
                ?: return Result.failure(IllegalStateException("Firebase not configured. Add google-services.json."))

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Sign-in failed"))

            val profile = UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: account.displayName ?: "Student",
                email = user.email ?: account.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: account.photoUrl?.toString()
            )
            saveProfileLocally(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfileLocally(profile: UserProfile) {
        val existing = userProfileDao.getProfile(profile.uid)
        userProfileDao.upsert(
            UserProfileEntity(
                uid = profile.uid,
                displayName = profile.displayName,
                email = profile.email,
                photoUrl = profile.photoUrl,
                gradeLevel = existing?.gradeLevel ?: profile.gradeLevel,
                subscriptionTier = existing?.subscriptionTier ?: profile.subscriptionTier,
                createdAt = existing?.createdAt ?: profile.createdAt,
                lastSignInAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateGradeLevel(uid: String, gradeLevel: Int) {
        val existing = userProfileDao.getProfile(uid) ?: return
        userProfileDao.upsert(existing.copy(gradeLevel = gradeLevel))
    }

    suspend fun updateSubscriptionTier(uid: String, tier: String) {
        val existing = userProfileDao.getProfile(uid) ?: return
        userProfileDao.upsert(existing.copy(subscriptionTier = tier))
    }

    suspend fun signOut() {
        auth?.signOut()
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().await()
    }

    private fun UserProfileEntity.toUserProfile() = UserProfile(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        gradeLevel = gradeLevel,
        subscriptionTier = subscriptionTier,
        createdAt = createdAt
    )
}
