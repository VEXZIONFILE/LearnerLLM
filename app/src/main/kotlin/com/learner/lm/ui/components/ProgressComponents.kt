package com.learner.lm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learner.lm.database.StudyTopicEntity
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun ProgressStreakHero(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.Accent,
                            Color(0xFF6366F1),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .padding(AppSpacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentStreak == 1) "1 day streak" else "$currentStreak day streak",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = streakMessage(currentStreak),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    if (longestStreak > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AppColors.ProGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Best: $longestStreak days",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(AppRadii.md))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AppColors.ProGold,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressStatsRow(
    topicCount: Int,
    averageMastery: Int,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        ProgressMiniStat(
            icon = Icons.Default.AutoStories,
            value = topicCount.toString(),
            label = "Topics",
            modifier = Modifier.weight(1f)
        )
        ProgressMiniStat(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            value = "$averageMastery%",
            label = "Mastery",
            modifier = Modifier.weight(1f)
        )
        ProgressMiniStat(
            icon = Icons.Default.LocalFireDepartment,
            value = reviewCount.toString(),
            label = "To review",
            modifier = Modifier.weight(1f),
            accent = if (reviewCount > 0) AppColors.ProGold else null
        )
    }
}

@Composable
private fun ProgressMiniStat(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    NotebookCard(modifier = modifier, elevated = false) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NotebookCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Surface(
                    shape = RoundedCornerShape(AppRadii.sm),
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun StudyTopicRow(
    topic: StudyTopicEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatLastStudied(topic.lastStudiedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SubjectChip(subject = topic.subject)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { topic.strengthScore.coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(AppRadii.pill)),
                color = strengthColor(topic.strengthScore),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = strengthLabel(topic.strengthScore),
                style = MaterialTheme.typography.labelSmall,
                color = strengthColor(topic.strengthScore),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(52.dp)
            )
        }
    }
}

@Composable
fun WeakTopicRow(
    topic: StudyTopicEntity,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.md),
        color = AppColors.ProGold.copy(alpha = 0.07f)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                SubjectChip(subject = topic.subject, muted = true)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { topic.strengthScore.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(AppRadii.pill)),
                    color = AppColors.ProGold,
                    trackColor = AppColors.ProGold.copy(alpha = 0.18f)
                )
                Text(
                    text = "${(topic.strengthScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.ProGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
private fun SubjectChip(
    subject: String,
    muted: Boolean = false
) {
    val color = subjectAccentColor(subject)
    Surface(
        shape = RoundedCornerShape(AppRadii.pill),
        color = color.copy(alpha = if (muted) 0.1f else 0.14f)
    ) {
        Text(
            text = formatSubjectLabel(subject),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (muted) AppColors.ProGold else color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProgressTopicDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        thickness = 0.5.dp
    )
}

private fun streakMessage(streak: Int): String = when {
    streak <= 0 -> "Chat today to start your streak"
    streak == 1 -> "Great start — come back tomorrow"
    streak < 7 -> "You're building a habit — keep it up!"
    streak < 30 -> "Impressive consistency this week"
    else -> "You're on fire — legendary dedication!"
}

private fun strengthColor(score: Float): Color = when {
    score >= 0.75f -> AppColors.Success
    score >= 0.5f -> AppColors.Accent
    else -> AppColors.ProGold
}

private fun strengthLabel(score: Float): String = when {
    score >= 0.75f -> "Strong"
    score >= 0.5f -> "Good"
    score >= 0.25f -> "Fair"
    else -> "Weak"
}

private fun formatSubjectLabel(subject: String): String =
    subject.replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.titlecase() }
        }

private fun subjectAccentColor(subject: String): Color {
    val palette = listOf(
        AppColors.Accent,
        Color(0xFF0891B2),
        Color(0xFF7C3AED),
        Color(0xFFDB2777),
        Color(0xFF059669),
        Color(0xFFEA580C)
    )
    val index = abs(subject.hashCode()) % palette.size
    return palette[index]
}

private fun formatLastStudied(timestamp: Long): String {
    val studiedDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(studiedDate, today)
    return when {
        days <= 0L -> "Studied today"
        days == 1L -> "Studied yesterday"
        days < 7L -> "Studied $days days ago"
        else -> "Studied on ${studiedDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
    }
}
