package com.learner.lm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.FreeModelVariant
import com.learner.lm.ai.learningBehavior
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.ui.components.AddCustomSubjectDialog
import com.learner.lm.ui.components.AppModePicker
import com.learner.lm.ui.components.ChatBubble
import com.learner.lm.ui.components.FreeModelPicker
import com.learner.lm.ui.components.HintLevelIndicator
import com.learner.lm.ui.components.LearnerLogo
import com.learner.lm.ui.components.NotebookBadge
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.components.PremiumUpgradeBanner
import com.learner.lm.ui.components.ReportAiContentDialog
import com.learner.lm.ui.components.SubjectPicker
import com.learner.lm.ui.components.TypingIndicator
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

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
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val isPremium = subscriptionTier == SubscriptionTier.BASIC.name ||
        subscriptionTier == SubscriptionTier.PRO.name
    val isFreeTier = subscriptionTier == SubscriptionTier.FREE.name
    val behaviorMode = uiState.selectedMode.learningBehavior(uiState.selectedFreeModel)
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    LaunchedEffect(imeBottom, uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty() || uiState.isLoading) {
            val target = if (uiState.isLoading) {
                uiState.messages.size
            } else {
                uiState.messages.lastIndex
            }
            listState.animateScrollToItem(target.coerceAtLeast(0))
        }
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                uiState.scannedText?.let { scanned ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        shape = RoundedCornerShape(AppRadii.md),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(
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

                if (!isPremium && !uiState.canSendMessage) {
                    NotebookCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        elevated = true
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Daily message limit reached",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when {
                                    uiState.subscriptionTier == SubscriptionTier.BASIC.name ->
                                        "Daily limit reached for this mode (500/day). Upgrade to Premium for unlimited messages."
                                    else ->
                                        "Daily limit reached for ${uiState.selectedFreeModel.quotaLabel} (60/day across all modes). Switch models or upgrade to Pro."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onNavigateToUpgrade,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppRadii.md)
                            ) {
                                Text("Upgrade for unlimited messages")
                            }
                        }
                    }
                }

                ChatComposer(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = inputPlaceholder(behaviorMode),
                    enabled = !uiState.isLoading && uiState.canSendMessage,
                    onSend = {
                        viewModel.sendMessage(input)
                        input = ""
                    }
                )
            }
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ChatHeader(
            activeModelLabel = uiState.activeModelLabel,
            selectedMode = uiState.selectedMode,
            behaviorMode = behaviorMode,
            hintLevel = uiState.hintLevel.level,
            sessionLabel = uiState.sessionLabel,
            messageQuotaLabel = uiState.messageQuotaLabel,
            isQuotaLoading = uiState.isQuotaLoading,
            isPremiumMessaging = uiState.isPremiumMessaging,
            hasMessages = uiState.messages.isNotEmpty(),
            onNewChat = viewModel::newChat,
            onClearChat = viewModel::clearChat
        )

        if (uiState.messages.isNotEmpty()) {
            ChatQuickActions(
                mode = behaviorMode,
                onAction = viewModel::sendQuickAction
            )
        }

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

        if (isFreeTier) {
            FreeModelPicker(
                selectedVariant = uiState.selectedFreeModel,
                onVariantSelected = viewModel::selectFreeModel
            )
        }

        SubjectPicker(
            selectedSubject = uiState.selectedSubject,
            customSubjects = uiState.customSubjects,
            onSubjectSelected = viewModel::selectSubject,
            onAddCustomSubject = viewModel::showAddSubjectDialog
        )

        if (!isPremium && showUpgradeBanner) {
            PremiumUpgradeBanner(
                onUpgrade = onNavigateToUpgrade,
                onDismiss = { showUpgradeBanner = false }
            )
        }

        if (uiState.messages.isNotEmpty()) {
            ChatQuickActions(
                mode = uiState.selectedMode,
                onAction = viewModel::sendQuickAction
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                item {
                    ChatEmptyState(
                        mode = uiState.selectedMode,
                        freeModelVariant = uiState.selectedFreeModel,
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
                    onCopy = if (message.role == "tutor") {
                        {
                            clipboardManager.setText(AnnotatedString(message.content))
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard")
                            }
                        }
                    } else {
                        null
                    },
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
    }
    }

    val errorMessage = uiState.error
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    if (uiState.showAddSubjectDialog) {
        AddCustomSubjectDialog(
            onDismiss = viewModel::dismissAddSubjectDialog,
            onConfirm = viewModel::addCustomSubject,
            error = uiState.error
        )
    }

    val reportTarget = uiState.reportTarget
    if (reportTarget != null) {
        ReportAiContentDialog(
            contentPreview = reportTarget.content,
            onDismiss = viewModel::dismissReportDialog,
            onSubmit = viewModel::submitReport,
            isSubmitting = uiState.isSubmittingReport
        )
    }

    val reportError = uiState.reportError
    if (reportError != null) {
        LaunchedEffect(reportError) {
            snackbarHostState.showSnackbar(reportError)
            viewModel.clearReportError()
        }
    }
}

@Composable
private fun ChatHeader(
    activeModelLabel: String,
    selectedMode: AppMode,
    behaviorMode: AppMode,
    hintLevel: Int,
    sessionLabel: String,
    messageQuotaLabel: String,
    isQuotaLoading: Boolean,
    isPremiumMessaging: Boolean,
    hasMessages: Boolean,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNewChat) {
            Icon(Icons.Default.Add, contentDescription = "New chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activeModelLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$sessionLabel · ${selectedMode.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (!isPremiumMessaging && messageQuotaLabel.isNotBlank()) {
                if (isQuotaLoading) {
                    Text(
                        text = "Checking message quota…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    NotebookBadge(
                        text = messageQuotaLabel,
                        highlighted = false,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        if (hasMessages) {
            IconButton(onClick = onClearChat) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Clear chat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (behaviorMode == AppMode.TUTOR) {
            HintLevelIndicator(level = hintLevel)
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatQuickActions(
    mode: AppMode,
    onAction: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickActionsForMode(mode).forEach { action ->
            SuggestionChip(text = action.label, onClick = { onAction(action.prompt) })
        }
    }
}

private data class QuickAction(val label: String, val prompt: String)

private fun quickActionsForMode(mode: AppMode): List<QuickAction> = when (mode) {
    AppMode.TUTOR -> listOf(
        QuickAction("Explain simpler", "Explain that in simpler terms for my grade level."),
        QuickAction("Give example", "Walk me through a similar example step by step."),
        QuickAction("Quiz me", "Quiz me with 3 questions to check my understanding.")
    )
    AppMode.STUDY -> listOf(
        QuickAction("More flashcards", "Add 5 more flashcards on this topic."),
        QuickAction("Practice quiz", "Give me a short practice quiz with an answer key."),
        QuickAction("Summarize", "Summarize the key points in bullet form.")
    )
    AppMode.CODE -> listOf(
        QuickAction("Explain line by line", "Explain the code line by line."),
        QuickAction("Find the bug", "Help me find the bug and explain why it happens."),
        QuickAction("Suggest fix", "Suggest a small fix and explain each change.")
    )
    AppMode.FREE -> quickActionsForMode(AppMode.TUTOR)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatEmptyState(
    mode: AppMode,
    freeModelVariant: FreeModelVariant,
    onSuggestionClick: (String) -> Unit
) {
    val behaviorMode = mode.learningBehavior(freeModelVariant)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "L",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = emptyStateTitle(mode, behaviorMode),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = emptyStateMessage(mode, behaviorMode),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionsForMode(behaviorMode).forEach { suggestion ->
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
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
    val dark = isSystemInDarkTheme()
    val borderColor = if (dark) AppColors.DarkComposerBorder else AppColors.ComposerBorder
    val fillColor = if (dark) AppColors.DarkComposerBackground else AppColors.ComposerBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadii.composer),
            color = fillColor,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    maxLines = 6,
                    enabled = enabled,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Surface(
                    shape = CircleShape,
                    color = if (canSend) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = onSend, enabled = canSend) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = "LearnerLM can make mistakes. Check important work with your teacher.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
        )
    }
}

private fun emptyStateTitle(mode: AppMode, behaviorMode: AppMode): String {
    val base = when (behaviorMode) {
        AppMode.TUTOR -> "How can I help you learn?"
        AppMode.STUDY -> "What do you want to study?"
        AppMode.CODE -> "What are you working on?"
        AppMode.FREE -> "What can I help you with?"
    }
    return if (mode == AppMode.FREE) "Free · $base" else base
}

private fun emptyStateMessage(mode: AppMode, behaviorMode: AppMode): String {
    val base = when (behaviorMode) {
        AppMode.TUTOR -> "Ask about homework or concepts. I'll guide you with hints and questions — never just the answer."
        AppMode.STUDY -> "Enter a topic to get a summary, key concepts, flashcards, and quiz questions."
        AppMode.CODE -> "Paste a snippet, error, or question. I'll help you debug and understand step by step."
        AppMode.FREE -> "Pick a free OpenRouter model above and start chatting."
    }
    return if (mode == AppMode.FREE) {
        "$base No subscription required — powered by OpenRouter free models."
    } else {
        base
    }
}

private fun inputPlaceholder(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask LearnerLM anything…"
    AppMode.STUDY -> "Enter a topic to study…"
    AppMode.CODE -> "Paste code or describe your bug…"
    AppMode.FREE -> "Message the free model…"
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
    AppMode.FREE -> listOf(
        "Explain photosynthesis simply",
        "Cell biology summary",
        "Debug my Python loop"
    )
}
