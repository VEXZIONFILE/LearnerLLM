package com.learner.lm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ai.AppMode
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.ui.components.AddCustomSubjectDialog
import com.learner.lm.ui.components.AppModePicker
import com.learner.lm.ui.components.ChatBubble
import com.learner.lm.ui.components.HintLevelIndicator
import com.learner.lm.ui.components.LearnerLogo
import com.learner.lm.ui.components.PremiumUpgradeBanner
import com.learner.lm.ui.components.ReportAiContentDialog
import com.learner.lm.ui.components.SubjectPicker
import com.learner.lm.ui.components.TypingIndicator
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    gradeLevel: Int,
    subscriptionTier: String,
    onNavigateToUpgrade: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var showUpgradeBanner by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isPremium = subscriptionTier == SubscriptionTier.BASIC.name ||
        subscriptionTier == SubscriptionTier.PRO.name

    LaunchedEffect(uiState.reportConfirmation) {
        uiState.reportConfirmation?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearReportConfirmation()
        }
    }

    LaunchedEffect(gradeLevel) {
        viewModel.setGradeLevel(gradeLevel)
    }

    LaunchedEffect(subscriptionTier) {
        viewModel.setSubscriptionTier(subscriptionTier)
    }

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty() || uiState.isLoading) {
            val target = if (uiState.isLoading) {
                uiState.messages.size
            } else {
                uiState.messages.lastIndex
            }
            listState.animateScrollToItem(target.coerceAtLeast(0))
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
    ) {
        ChatHeader(
            activeModelLabel = uiState.activeModelLabel,
            selectedMode = uiState.selectedMode,
            hintLevel = uiState.hintLevel.level
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        if (!isPremium && showUpgradeBanner) {
            PremiumUpgradeBanner(
                onUpgrade = onNavigateToUpgrade,
                onDismiss = { showUpgradeBanner = false }
            )
        }

        AppModePicker(
            selectedMode = uiState.selectedMode,
            onModeSelected = viewModel::selectMode
        )

        SubjectPicker(
            selectedSubject = uiState.selectedSubject,
            customSubjects = uiState.customSubjects,
            onSubjectSelected = viewModel::selectSubject,
            onAddCustomSubject = viewModel::showAddSubjectDialog
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = AppSpacing.md,
                vertical = AppSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                item {
                    ChatEmptyState(
                        mode = uiState.selectedMode,
                        onSuggestionClick = { suggestion ->
                            viewModel.sendMessage(suggestion)
                        }
                    )
                }
            }
            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(
                    message = message.content,
                    isStudent = message.role == "student",
                    onReport = if (message.role == "tutor") {
                        {
                            viewModel.openReportDialog(
                                messageId = message.id,
                                content = message.content
                            )
                        }
                    } else {
                        null
                    }
                )
            }
            if (uiState.isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        uiState.scannedText?.let { scanned ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                shape = RoundedCornerShape(AppRadii.md),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Homework attached",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = scanned.take(120) + if (scanned.length > 120) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        ChatComposer(
            value = input,
            onValueChange = { input = it },
            placeholder = inputPlaceholder(uiState.selectedMode),
            enabled = !uiState.isLoading,
            onSend = {
                viewModel.sendMessage(input)
                input = ""
            }
        )
    }
    }

    if (uiState.showAddSubjectDialog) {
        AddCustomSubjectDialog(
            onDismiss = viewModel::dismissAddSubjectDialog,
            onConfirm = viewModel::addCustomSubject,
            error = uiState.error
        )
    }

    uiState.reportTarget?.let { target ->
        ReportAiContentDialog(
            contentPreview = target.content,
            onDismiss = viewModel::dismissReportDialog,
            onSubmit = viewModel::submitReport,
            isSubmitting = uiState.isSubmittingReport
        )
    }

    uiState.reportError?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearReportError()
        }
    }
}

@Composable
private fun ChatHeader(
    activeModelLabel: String,
    selectedMode: AppMode,
    hintLevel: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = AppSpacing.md, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = activeModelLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = selectedMode.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (selectedMode == AppMode.TUTOR) {
            HintLevelIndicator(level = hintLevel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatEmptyState(
    mode: AppMode,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Surface(
            shape = RoundedCornerShape(AppRadii.xl),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            LearnerLogo(
                showWordmark = false,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }
        Text(
            text = emptyStateTitle(mode),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = emptyStateMessage(mode),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppSpacing.md)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionsForMode(mode).forEach { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.pill),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    onSend: () -> Unit
) {
    val canSend = value.isNotBlank() && enabled
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(AppRadii.xl)
                        ),
                    placeholder = {
                        Text(
                            placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    },
                    maxLines = 5,
                    shape = RoundedCornerShape(AppRadii.xl),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.size(AppSpacing.sm))
                Surface(
                    shape = CircleShape,
                    color = if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = onSend,
                        enabled = canSend
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            Text(
                text = "LearnerLM can make mistakes. Verify important answers with your teacher.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
            )
        }
    }
}

private fun emptyStateTitle(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "How can I help you learn?"
    AppMode.STUDY -> "What do you want to study?"
    AppMode.CODE -> "What are you working on?"
}

private fun emptyStateMessage(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask about homework or concepts. I'll guide you with hints and questions — never just the answer."
    AppMode.STUDY -> "Enter a topic to get a summary, key concepts, flashcards, and quiz questions."
    AppMode.CODE -> "Paste a snippet, error, or question. I'll help you debug and understand step by step."
}

private fun inputPlaceholder(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask LearnerLM anything…"
    AppMode.STUDY -> "Enter a topic to study…"
    AppMode.CODE -> "Paste code or describe your bug…"
}

private fun suggestionsForMode(mode: AppMode): List<String> = when (mode) {
    AppMode.TUTOR -> listOf(
        "Explain photosynthesis simply",
        "Help me with quadratic equations",
        "What caused World War I?"
    )
    AppMode.STUDY -> listOf(
        "Cell biology summary",
        "US history flashcards",
        "Spanish verb conjugation"
    )
    AppMode.CODE -> listOf(
        "Debug my Python loop",
        "Explain recursion",
        "Fix this Java error"
    )
}
