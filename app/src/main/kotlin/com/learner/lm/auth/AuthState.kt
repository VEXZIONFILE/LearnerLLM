package com.learner.lm.auth

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val gradeLevel: Int = 8,
    val subscriptionTier: String = "FREE",
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthState {
    data object Loading : AuthState()
    data object SignedOut : AuthState()
    data class SignedIn(val profile: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}
