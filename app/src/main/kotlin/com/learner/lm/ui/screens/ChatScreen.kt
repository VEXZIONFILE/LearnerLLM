package com.learner.lm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ai.AiConfig
import com.learner.lm.ui.components.AddCustomSubjectDialog
import com.learner.lm.ui.components.ChatBubble
import com.learner.lm.ui.components.HintLevelIndicator
import com.learner.lm.ui.components.SubjectPicker
import com.learner.lm.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Learner LM 🎓", fontWeight = FontWeight.Bold)
                        Text(
                            "Grade ${uiState.gradeLevel} · ${uiState.selectedSubject.displayName} · ${AiConfig.MODEL_DISPLAY_NAME}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HintLevelIndicator(level = uiState.hintLevel.level)
                Text(
                    text = "Socratic tutoring — no direct answers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            SubjectPicker(
                selectedSubject = uiState.selectedSubject,
                customSubjects = uiState.customSubjects,
                onSubjectSelected = viewModel::selectSubject,
                onAddCustomSubject = viewModel::showAddSubjectDialog
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Grade Level: ${uiState.gradeLevel}")
                Slider(
                    value = uiState.gradeLevel.toFloat(),
                    onValueChange = { viewModel.setGradeLevel(it.toInt()) },
                    valueRange = 6f..12f,
                    steps = 5
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.messages.isEmpty()) {
                    item {
                        Text(
                            text = "Ask me anything about your homework — I'll guide you with hints and questions, never direct answers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 24.dp)
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
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            uiState.scannedText?.let { scanned ->
                Text(
                    text = "📷 Scanned: ${scanned.take(80)}...",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask a question or describe your problem...") },
                    maxLines = 4
                )
                IconButton(
                    onClick = {
                        viewModel.sendMessage(input)
                        input = ""
                    },
                    enabled = input.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
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

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: com.learner.lm.viewmodel.ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Progress Tracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        com.learner.lm.ui.components.StreakBadge(streak = uiState.streak?.currentStreak ?: 0)

        Text("Studied Topics", style = MaterialTheme.typography.titleMedium)
        if (uiState.topics.isEmpty()) {
            Text("Start chatting to track your learning topics.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            uiState.topics.forEach { topic ->
                Text("• ${topic.name} (${topic.subject})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Areas to Review", style = MaterialTheme.typography.titleMedium)
        if (uiState.weakTopics.isEmpty()) {
            Text("No weaknesses detected yet — keep learning!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            uiState.weakTopics.forEach { topic ->
                Text("⚠️ ${topic.name}")
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Homework Scanner", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Point your camera at a worksheet or textbook page. ML Kit OCR will extract the text for guided tutoring.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera integration ready — connect HomeworkScanner in your activity result handler.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
