package com.learner.lm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.learner.lm.ui.components.ChatBubble
import com.learner.lm.ui.components.ChatSettingsSheet
import com.learner.lm.ui.components.LearnerLogo
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.components.TypingIndicator
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    var showSettings by remember { mutableStateOf(false) }
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
            val target = if (uiState.isLoading) uiState.messages.size else uiState.messages.lastIndex
            listState.animateScrollToItem(target.coerceAtLeast(0))
        }
    }

    LaunchedEffect(uiState.reportConfirmation) {
        uiState.reportConfirmation?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearReportConfirmation()
        }
    }

    LaunchedEffect(gradeLevel) { viewModel.setGradeLevel(gradeLevel) }
    LaunchedEffect(subscriptionTier) { viewModel.setSubscriptionTier(subscriptionTier) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                modelLabel = uiState.activeModelLabel,
                modeLabel = uiState.selectedMode.shortLabel,
                hasMessages = uiState.messages.isNotEmpty(),
                onNewChat = viewModel::newChat,
                onClearChat = viewModel::clearChat,
                onOpenSettings = { showSettings = true }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (uiState.isOfflineMode) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "Offline mode — basic tutor responses only. Reconnect for full AI.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 8.dp)
                        )
                    }
                }

                uiState.scannedText?.let { scanned ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = 4.dp),
                        shape = RoundedCornerShape(AppRadii.md),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "📎 ${scanned.take(100)}${if (scanned.length > 100) "…" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (!isPremium && !uiState.canSendMessage) {
                    NotebookCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = 4.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Daily limit reached",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Button(
                                onClick = onNavigateToUpgrade,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppRadii.md)
                            ) {
                                Text("Upgrade")
                            }
                        }
                    }
                }

                ChatComposer(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = inputPlaceholder(behaviorMode),
                    enabled = !uiState.isLoading && uiState.canSendMessage,
                    modeLabel = uiState.selectedMode.shortLabel,
                    onOpenSettings = { showSettings = true },
                    onSend = {
                        viewModel.sendMessage(input)
                        input = ""
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                ChatEmptyState(
                    mode = behaviorMode,
                    onSuggestionClick = viewModel::sendMessage,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            ChatBubble(
                                message = message.content,
                                isStudent = message.role == "student",
                                modifier = Modifier.widthIn(max = AppSpacing.chatMaxWidth),
                                onCopy = if (message.role == "tutor") {
                                    {
                                        clipboardManager.setText(AnnotatedString(message.content))
                                        scope.launch { snackbarHostState.showSnackbar("Copied") }
                                    }
                                } else null,
                                onReport = if (message.role == "tutor") {
                                    {
                                        viewModel.openReportDialog(message.id, message.content)
                                    }
                                } else null
                            )
                        }
                    }
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                TypingIndicator(modifier = Modifier.widthIn(max = AppSpacing.chatMaxWidth))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        ChatSettingsSheet(
            selectedMode = uiState.selectedMode,
            selectedFreeModel = uiState.selectedFreeModel,
            selectedSubject = uiState.selectedSubject,
            customSubjects = uiState.customSubjects,
            isFreeTier = isFreeTier,
            messageQuotaLabel = uiState.messageQuotaLabel,
            onModeSelected = viewModel::selectMode,
            onFreeModelSelected = viewModel::selectFreeModel,
            onSubjectSelected = viewModel::selectSubject,
            onAddCustomSubject = viewModel::showAddSubjectDialog,
            onDismiss = { showSettings = false }
        )
    }

    uiState.error?.let { errorMessage ->
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

    uiState.reportTarget?.let { target ->
        com.learner.lm.ui.components.ReportAiContentDialog(
            contentPreview = target.content,
            onDismiss = viewModel::dismissReportDialog,
            onSubmit = viewModel::submitReport,
            isSubmitting = uiState.isSubmittingReport
        )
    }

    uiState.reportError?.let { reportError ->
        LaunchedEffect(reportError) {
            snackbarHostState.showSnackbar(reportError)
            viewModel.clearReportError()
        }
    }
}

@Composable
private fun ChatTopBar(
    modelLabel: String,
    modeLabel: String,
    hasMessages: Boolean,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNewChat) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = modelLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Tune, contentDescription = "Settings")
            }
            if (hasMessages) {
                IconButton(onClick = onClearChat) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear chat")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatEmptyState(
    mode: AppMode,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = AppSpacing.chatMaxWidth)
            .padding(horizontal = AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        LearnerLogo(
            showWordmark = false,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = emptyStateTitle(mode),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = emptyStateMessage(mode),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionsForMode(mode).forEach { suggestion ->
                SuggestionChip(text = suggestion, onClick = { onSuggestionClick(suggestion) })
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.pill),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modeLabel: String,
    onOpenSettings: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(AppRadii.pill),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = modeLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = AppSpacing.chatMaxWidth),
                shape = RoundedCornerShape(AppRadii.composer),
                color = fillColor,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = 2.dp
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
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
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
                    color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = onSend, enabled = canSend) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            }
        }

        Text(
            text = "LearnerLM can make mistakes. Verify important work.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

private fun emptyStateTitle(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "How can I help you learn?"
    AppMode.STUDY -> "What do you want to study?"
    AppMode.CODE -> "What are you working on?"
    AppMode.FREE -> "What can I help you with?"
}

private fun emptyStateMessage(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask anything. I'll guide you step by step."
    AppMode.STUDY -> "Get summaries, flashcards, and quizzes on any topic."
    AppMode.CODE -> "Paste code or describe a bug — I'll help you debug it."
    AppMode.FREE -> "Choose a model in settings and start chatting."
}

private fun inputPlaceholder(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Message LearnerLM…"
    AppMode.STUDY -> "Enter a topic…"
    AppMode.CODE -> "Paste code or describe your issue…"
    AppMode.FREE -> "Send a message…"
}

private fun suggestionsForMode(mode: AppMode): List<String> = when (mode) {
    AppMode.TUTOR -> listOf("Explain photosynthesis", "Help with algebra", "What caused WWI?")
    AppMode.STUDY -> listOf("Cell biology summary", "US history flashcards", "Spanish verbs")
    AppMode.CODE -> listOf("Debug my Python loop", "Explain recursion", "Fix this error")
    AppMode.FREE -> listOf("Explain photosynthesis", "Study cell biology", "Debug Python code")
}
