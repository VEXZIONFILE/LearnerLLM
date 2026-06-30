package com.learner.lm.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
    var currentDestination by remember { mutableStateOf(AppDestination.Chat) }

    when (val state = authState) {
        AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
            val profile = state.profile
            NotebookScaffold(
                currentDestination = currentDestination,
                userProfile = profile,
                onNavigate = { currentDestination = it },
                onSignOut = { authViewModel.signOut() }
            ) {
                when (currentDestination) {
                    AppDestination.Chat -> ChatScreen(viewModel = chatViewModel)
                    AppDestination.Scanner -> ScannerScreen(
                        onTextScanned = { chatViewModel.setScannedText(it) }
                    )
                    AppDestination.Progress -> ProgressScreen()
                    AppDestination.Profile -> ProfileScreen(
                        profile = profile,
                        onGradeLevelChange = { grade ->
                            authViewModel.updateGradeLevel(profile, grade)
                        },
                        onNavigateToSubscription = { currentDestination = AppDestination.Subscription }
                    )
                    AppDestination.Subscription -> SubscriptionScreen(
                        userProfile = profile,
                        billingViewModel = billingViewModel
                    )
                    AppDestination.Login -> Unit
                }
            }
        }
        is AuthState.Error -> LoginScreen(authViewModel = authViewModel)
    }
}
