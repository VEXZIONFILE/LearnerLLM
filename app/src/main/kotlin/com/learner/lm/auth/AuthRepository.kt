package com.learner.lm.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.learner.lm.database.UserProfileDao
import com.learner.lm.database.UserProfileEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(AuthState.Error("Firebase not configured. Add google-services.json."))
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { authInstance ->
            val user = authInstance.currentUser
            if (user == null) {
                trySend(AuthState.SignedOut)
            } else {
                val profile = runBlocking { buildProfile(user) }
                trySend(AuthState.SignedIn(profile))
            }
        }

        firebaseAuth.currentUser?.let { user ->
            trySend(AuthState.SignedIn(runBlocking { buildProfile(user) }))
        } ?: trySend(AuthState.SignedOut)

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun observeLocalProfile(uid: String): Flow<UserProfile?> =
        userProfileDao.observeProfile(uid).map { entity ->
            entity?.toUserProfile()
        }

    suspend fun signUp(email: String, password: String, displayName: String): Result<UserProfile> {
        return try {
            val firebaseAuth = auth
                ?: return Result.failure(IllegalStateException("Firebase not configured. Add google-services.json."))

            val trimmedEmail = email.trim()
            val trimmedName = displayName.trim()
            require(trimmedEmail.contains("@")) { "Enter a valid email address" }
            require(password.length >= 6) { "Password must be at least 6 characters" }
            require(trimmedName.isNotBlank()) { "Enter your name" }

            val result = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("Account creation failed"))

            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedName)
                    .build()
            ).await()

            val profile = buildProfile(user, trimmedName)
            saveProfileLocally(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyAuthMessage(e)))
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return try {
            val firebaseAuth = auth
                ?: return Result.failure(IllegalStateException("Firebase not configured. Add google-services.json."))

            val trimmedEmail = email.trim()
            require(trimmedEmail.contains("@")) { "Enter a valid email address" }
            require(password.isNotBlank()) { "Enter your password" }

            val result = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("Sign-in failed"))

            val profile = buildProfile(user)
            saveProfileLocally(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyAuthMessage(e)))
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
    }

    private suspend fun buildProfile(user: FirebaseUser, overrideName: String? = null): UserProfile {
        val local = userProfileDao.getProfile(user.uid)
        val name = overrideName
            ?: local?.displayName
            ?: user.displayName
            ?: user.email?.substringBefore("@")
            ?: "Student"
        return UserProfile(
            uid = user.uid,
            displayName = name,
            email = user.email.orEmpty(),
            photoUrl = user.photoUrl?.toString(),
            gradeLevel = local?.gradeLevel ?: 8,
            subscriptionTier = local?.subscriptionTier ?: "FREE",
            createdAt = local?.createdAt ?: System.currentTimeMillis()
        )
    }

    private fun friendlyAuthMessage(error: Exception): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address."
            message.contains("password is invalid", ignoreCase = true) ||
                message.contains("wrong-password", ignoreCase = true) ->
                "Incorrect email or password."
            message.contains("no user record", ignoreCase = true) ||
                message.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email. Try signing up."
            message.contains("email address is already in use", ignoreCase = true) ->
                "An account with this email already exists. Try signing in."
            message.contains("weak password", ignoreCase = true) ->
                "Password must be at least 6 characters."
            message.contains("network", ignoreCase = true) ->
                "Network error. Check your connection and try again."
            else -> message.ifBlank { "Authentication failed. Please try again." }
        }
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
