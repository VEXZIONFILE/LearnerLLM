package com.learner.lm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ui.components.EmptyStateCard
import com.learner.lm.ui.components.ProgressSectionCard
import com.learner.lm.ui.components.ProgressStatsRow
import com.learner.lm.ui.components.ProgressStreakHero
import com.learner.lm.ui.components.ProgressTopicDivider
import com.learner.lm.ui.components.StudyTopicRow
import com.learner.lm.ui.components.WeakTopicRow
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.ProgressViewModel
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStreak = uiState.streak?.currentStreak ?: 0
    val longestStreak = uiState.streak?.longestStreak ?: 0
    val averageMastery = if (uiState.topics.isEmpty()) {
        0
    } else {
        (uiState.topics.map { it.strengthScore }.average() * 100).roundToInt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Your progress",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "See what you've studied and where to focus next.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProgressStreakHero(
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )

        ProgressStatsRow(
            topicCount = uiState.topics.size,
            averageMastery = averageMastery,
            reviewCount = uiState.weakTopics.size
        )

        ProgressSectionCard(
            title = "Studied topics",
            icon = Icons.Default.Lightbulb,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            if (uiState.topics.isEmpty()) {
                EmptyStateCard(
                    title = "No topics yet",
                    message = "Start chatting with the tutor to track subjects you explore."
                )
            } else {
                uiState.topics.forEachIndexed { index, topic ->
                    if (index > 0) {
                        ProgressTopicDivider()
                    }
                    StudyTopicRow(topic = topic)
                }
            }
        }

        ProgressSectionCard(
            title = "Areas to review",
            icon = Icons.Default.WarningAmber,
            iconTint = MaterialTheme.colorScheme.tertiary
        ) {
            if (uiState.weakTopics.isEmpty()) {
                EmptyStateCard(
                    title = "All caught up",
                    message = "No weak spots detected — keep learning to build your mastery."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.weakTopics.forEach { topic ->
                        WeakTopicRow(topic = topic)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
