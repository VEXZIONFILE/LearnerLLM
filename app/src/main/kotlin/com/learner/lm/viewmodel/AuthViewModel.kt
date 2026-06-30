package com.learner.lm.viewmodel

import android.app.Application
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearError() {
        _authError.value = null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            authRepository.signIn(email, password)
                .onFailure { _authError.value = it.message }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            authRepository.signUp(email, password, displayName)
                .onFailure { _authError.value = it.message }
            _isLoading.value = false
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
