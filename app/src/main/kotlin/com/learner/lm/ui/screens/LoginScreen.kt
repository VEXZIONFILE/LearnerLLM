package com.learner.lm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToSubscription: () -> Unit = {}
) {
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val authError by authViewModel.authError.collectAsStateWithLifecycle()

    var isSignUp by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Learner LM",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = if (isSignUp) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        NotebookCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isSignUp) "Sign up to save your progress" else "Sign in to continue learning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "You'll stay signed in even after closing the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Your name") },
                placeholder = { Text("e.g. Alex") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; authViewModel.clearError() },
            label = { Text("Email") },
            placeholder = { Text("you@school.edu") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; authViewModel.clearError() },
            label = { Text("Password") },
            placeholder = { Text(if (isSignUp) "At least 6 characters" else "Your password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        authError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isSignUp) {
                    authViewModel.signUp(email, password, displayName)
                } else {
                    authViewModel.signIn(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (isSignUp) "Create account" else "Sign in",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        TextButton(
            onClick = {
                isSignUp = !isSignUp
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isSignUp) "Already have an account? Sign in"
                else "Don't have an account? Sign up"
            )
        }

        OutlinedButton(
            onClick = onNavigateToSubscription,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("View plans")
        }
    }
}
