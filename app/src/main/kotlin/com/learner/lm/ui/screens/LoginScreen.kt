package com.learner.lm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ui.components.BrandMark
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.AuthViewModel

private enum class AuthTab { SignIn, SignUp }

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    configError: String? = null,
    onNavigateToSubscription: () -> Unit = {}
) {
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val authError by authViewModel.authError.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isSignUp = selectedTab == AuthTab.SignUp.ordinal
    val firebaseReady = configError == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        AuthHeroHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            configError?.let { error ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.lg),
                    shape = RoundedCornerShape(AppRadii.md),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(AppSpacing.md),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg),
                shape = RoundedCornerShape(AppRadii.xl),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    height = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == AuthTab.SignIn.ordinal,
                            onClick = {
                                selectedTab = AuthTab.SignIn.ordinal
                                authViewModel.clearError()
                            },
                            text = {
                                Text(
                                    "Sign In",
                                    fontWeight = if (selectedTab == AuthTab.SignIn.ordinal) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    }
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == AuthTab.SignUp.ordinal,
                            onClick = {
                                selectedTab = AuthTab.SignUp.ordinal
                                authViewModel.clearError()
                            },
                            text = {
                                Text(
                                    "Sign Up",
                                    fontWeight = if (selectedTab == AuthTab.SignUp.ordinal) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    }
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    Text(
                        text = if (isSignUp) "Create your LearnerLM account" else "Welcome back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isSignUp) {
                            "Start with AI tutoring, study packs, and code help."
                        } else {
                            "Sign in to continue with your AI tutor."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = AppSpacing.md)
                    )

                    if (isSignUp) {
                        AuthTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Full name",
                            placeholder = "Alex Johnson",
                            enabled = firebaseReady
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; authViewModel.clearError() },
                        label = "Email",
                        placeholder = "you@school.edu",
                        keyboardType = KeyboardType.Email,
                        enabled = firebaseReady
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; authViewModel.clearError() },
                        label = "Password",
                        placeholder = if (isSignUp) "At least 6 characters" else "Your password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        enabled = firebaseReady
                    )

                    authError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

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
                        shape = RoundedCornerShape(AppRadii.md),
                        enabled = !isLoading && firebaseReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isSignUp) "Create account" else "Sign in",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!isSignUp) {
                        TextButton(
                            onClick = { selectedTab = AuthTab.SignUp.ordinal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            enabled = firebaseReady
                        ) {
                            Text(
                                "Don't have an account? Sign up",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { selectedTab = AuthTab.SignIn.ordinal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            enabled = firebaseReady
                        ) {
                            Text(
                                "Already have an account? Sign in",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            OutlinedButton(
                onClick = onNavigateToSubscription,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadii.md)
            ) {
                Text("View plans", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(AppSpacing.xl))
        }
    }
}

@Composable
private fun AuthHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.Accent.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xl)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(iconSize = 48.dp, showWordmark = true)
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = "Your AI learning copilot",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tutor · Study · Code — powered by frontier models",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuthFeatureChip(
                    icon = Icons.Default.School,
                    label = "Socratic tutor",
                    modifier = Modifier.weight(1f)
                )
                AuthFeatureChip(
                    icon = Icons.Default.AutoAwesome,
                    label = "Study packs",
                    modifier = Modifier.weight(1f)
                )
                AuthFeatureChip(
                    icon = Icons.Default.Code,
                    label = "Code help",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AuthFeatureChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadii.md),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword, enabled = enabled) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
