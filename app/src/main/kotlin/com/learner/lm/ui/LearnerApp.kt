package com.learner.lm.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.auth.AuthState
import com.learner.lm.ui.components.NotebookScaffold
import com.learner.lm.ui.navigation.AppDestination
import com.learner.lm.ui.screens.ChatScreen
import com.learner.lm.ui.screens.LoginScreen
import com.learner.lm.ui.screens.ProfileScreen
import com.learner.lm.ui.screens.ProgressScreen
import com.learner.lm.ui.screens.ScannerScreen
import com.learner.lm.ui.screens.SubscriptionScreen
import com.learner.lm.viewmodel.AuthViewModel
import com.learner.lm.viewmodel.BillingViewModel
import com.learner.lm.viewmodel.ChatViewModel

@Composable
fun LearnerApp() {
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val billingViewModel: BillingViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    var currentDestination by remember { mutableStateOf(AppDestination.Chat) }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.SignedOut, is AuthState.Error -> {
                currentDestination = AppDestination.Login
            }
            is AuthState.SignedIn -> {
                if (currentDestination == AppDestination.Login) {
                    currentDestination = AppDestination.Chat
                }
            }
            AuthState.Loading -> Unit
        }
    }

    when (val state = authState) {
        AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        AuthState.SignedOut -> {
            if (currentDestination == AppDestination.Subscription) {
                SubscriptionScreen(
                    userProfile = null,
                    billingViewModel = billingViewModel,
                    onBack = { currentDestination = AppDestination.Login }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToSubscription = { currentDestination = AppDestination.Subscription }
                )
            }
        }
        is AuthState.SignedIn -> {
            val profile = userProfile ?: state.profile

            LaunchedEffect(profile.gradeLevel, profile.subscriptionTier) {
                chatViewModel.setGradeLevel(profile.gradeLevel)
                chatViewModel.setSubscriptionTier(profile.subscriptionTier)
            }

            val isSubscription = currentDestination == AppDestination.Subscription

            NotebookScaffold(
                currentDestination = currentDestination,
                userProfile = profile,
                onNavigate = { currentDestination = it },
                showBack = isSubscription,
                onBack = if (isSubscription) {
                    { currentDestination = AppDestination.Profile }
                } else null
            ) {
                when (currentDestination) {
                    AppDestination.Chat -> ChatScreen(
                        gradeLevel = profile.gradeLevel,
                        subscriptionTier = profile.subscriptionTier,
                        onNavigateToUpgrade = { currentDestination = AppDestination.Subscription },
                        viewModel = chatViewModel
                    )
                    AppDestination.Scanner -> ScannerScreen(
                        userId = profile.uid,
                        subscriptionTier = profile.subscriptionTier,
                        onTextScanned = { text ->
                            chatViewModel.setScannedText(text)
                            currentDestination = AppDestination.Chat
                        },
                        onNavigateToUpgrade = { currentDestination = AppDestination.Subscription }
                    )
                    AppDestination.Progress -> ProgressScreen()
                    AppDestination.Profile -> ProfileScreen(
                        profile = profile,
                        onGradeLevelChange = { grade ->
                            authViewModel.updateGradeLevel(profile, grade)
                            chatViewModel.setGradeLevel(grade)
                        },
                        onNavigateToSubscription = { currentDestination = AppDestination.Subscription },
                        onSignOut = {
                            authViewModel.signOut()
                            currentDestination = AppDestination.Login
                        }
                    )
                    AppDestination.Subscription -> SubscriptionScreen(
                        userProfile = profile,
                        billingViewModel = billingViewModel,
                        onBack = { currentDestination = AppDestination.Profile }
                    )
                    AppDestination.Login -> Unit
                }
            }
        }
        is AuthState.Error -> {
            if (currentDestination == AppDestination.Subscription) {
                SubscriptionScreen(
                    userProfile = null,
                    billingViewModel = billingViewModel,
                    onBack = { currentDestination = AppDestination.Login }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    configError = state.message,
                    onNavigateToSubscription = { currentDestination = AppDestination.Subscription }
                )
            }
        }
    }
}
