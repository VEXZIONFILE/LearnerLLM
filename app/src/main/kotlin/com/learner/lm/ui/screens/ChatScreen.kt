package com.learner.lm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ai.AppMode
import com.learner.lm.ui.components.AddCustomSubjectDialog
import com.learner.lm.ui.components.AppModePicker
import com.learner.lm.ui.components.ChatBubble
import com.learner.lm.ui.components.EmptyStateCard
import com.learner.lm.ui.components.HintLevelIndicator
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.components.StreakBadge
import com.learner.lm.ui.components.SubjectPicker
import com.learner.lm.viewmodel.ChatViewModel
import com.learner.lm.viewmodel.ProgressViewModel

@Composable
fun ChatScreen(
    gradeLevel: Int,
    subscriptionTier: String,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(gradeLevel) {
        viewModel.setGradeLevel(gradeLevel)
    }

    LaunchedEffect(subscriptionTier) {
        viewModel.setSubscriptionTier(subscriptionTier)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.selectedMode == AppMode.TUTOR) {
                HintLevelIndicator(level = uiState.hintLevel.level)
            } else {
                Text(
                    text = uiState.selectedMode.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "${uiState.activeModelLabel} · Grade $gradeLevel · ${uiState.selectedSubject.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AppModePicker(
            selectedMode = uiState.selectedMode,
            onModeSelected = viewModel::selectMode,
            modifier = Modifier.padding(bottom = 8.dp)
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
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = emptyStateTitle(uiState.selectedMode),
                        message = emptyStateMessage(uiState.selectedMode),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp)
                    )
                }
            }
            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(
                    message = message.content,
                    isStudent = message.role == "student"
                )
            }
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }

        uiState.scannedText?.let { scanned ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Scanned: ${scanned.take(80)}${if (scanned.length > 80) "…" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = {
                    Text(
                        inputPlaceholder(uiState.selectedMode),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Surface(
                shape = CircleShape,
                color = if (input.isNotBlank() && !uiState.isLoading) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.padding(4.dp)
            ) {
                IconButton(
                    onClick = {
                        viewModel.sendMessage(input)
                        input = ""
                    },
                    enabled = input.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank() && !uiState.isLoading) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }

    if (uiState.showAddSubjectDialog) {
        AddCustomSubjectDialog(
            onDismiss = viewModel::dismissAddSubjectDialog,
            onConfirm = viewModel::addCustomSubject,
            error = uiState.error
        )
    }
}

private fun emptyStateTitle(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Start learning"
    AppMode.STUDY -> "Generate a study pack"
    AppMode.CODE -> "Get code help"
}

private fun emptyStateMessage(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask about your homework — I'll guide you with hints and questions, never direct answers."
    AppMode.STUDY -> "Enter a topic to get a summary, key concepts, flashcards, and quiz questions."
    AppMode.CODE -> "Paste a function, error, or small code snippet — I'll debug and explain step-by-step."
}

private fun inputPlaceholder(mode: AppMode): String = when (mode) {
    AppMode.TUTOR -> "Ask a question…"
    AppMode.STUDY -> "Enter a topic to study…"
    AppMode.CODE -> "Paste code or describe the bug…"
}

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your progress",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        StreakBadge(streak = uiState.streak?.currentStreak ?: 0)

        NotebookCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Studied topics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (uiState.topics.isEmpty()) {
                    Text(
                        text = "Start chatting to track your learning topics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.topics.forEach { topic ->
                        Text(
                            text = "${topic.name} · ${topic.subject}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        NotebookCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Areas to review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (uiState.weakTopics.isEmpty()) {
                    Text(
                        text = "No weaknesses detected yet — keep learning!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.weakTopics.forEach { topic ->
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerScreen(
    onTextScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Homework scanner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        NotebookCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "Scan your worksheet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Point your camera at a worksheet or textbook page. ML Kit OCR will extract the text for guided tutoring.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Camera integration connects via HomeworkScanner in your activity.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
