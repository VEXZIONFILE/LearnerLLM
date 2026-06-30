package com.learner.lm.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.LearnerLMApplication
import com.learner.lm.auth.AuthRepository
import com.learner.lm.auth.AuthState
import com.learner.lm.auth.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as LearnerLMApplication).database
    private val authRepository = AuthRepository(
        context = application,
        userProfileDao = database.userProfileDao()
    )

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    suspend fun createGoogleSignInIntent(activity: android.app.Activity): Intent =
        authRepository.getGoogleSignInIntent(activity)

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isSigningIn.value = true
            authRepository.handleGoogleSignInResult(data)
            _isSigningIn.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun updateGradeLevel(profile: UserProfile, gradeLevel: Int) {
        viewModelScope.launch {
            authRepository.updateGradeLevel(profile.uid, gradeLevel)
        }
    }
}
