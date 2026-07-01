package com.learner.lm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learner.lm.auth.UserProfile
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.ui.components.NotebookBadge
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.components.ProfileAvatar
import com.learner.lm.ui.components.ProfileStatCard
import com.learner.lm.ui.components.SectionHeader
import com.learner.lm.ui.components.SettingsRow
import com.learner.lm.ui.theme.AppSpacing

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onGradeLevelChange: (Int) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Manage your account and learning preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NotebookCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    name = profile.displayName,
                    photoUrl = profile.photoUrl,
                    size = 56.dp,
                    modifier = Modifier.clip(RoundedCornerShape(28.dp))
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPremiumTier(profile.subscriptionTier)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        NotebookBadge(text = "Premium", highlighted = true)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "Grade level",
                value = "${profile.gradeLevel}",
                modifier = Modifier.weight(1f)
            )
            if (isPremiumTier(profile.subscriptionTier)) {
                ProfileStatCard(
                    label = "Plan",
                    value = "Premium",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionHeader(title = "Learning")
        NotebookCard {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Grade level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Responses adapt to grade ${profile.gradeLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = profile.gradeLevel.toFloat(),
                    onValueChange = { onGradeLevelChange(it.toInt()) },
                    valueRange = 6f..12f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("6", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        SectionHeader(title = "Billing")
        NotebookCard {
            Column {
                SettingsRow(
                    icon = Icons.Default.Star,
                    title = "Subscription",
                    subtitle = subscriptionDescription(profile.subscriptionTier),
                    trailing = "Manage",
                    onClick = onNavigateToSubscription
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                SettingsRow(
                    icon = Icons.Default.Grade,
                    title = "Grade ${profile.gradeLevel}",
                    subtitle = "How advanced explanations should be"
                )
            }
        }

        SectionHeader(title = "Safety & AI")
        NotebookCard {
            SettingsRow(
                icon = Icons.Default.Flag,
                title = "Report AI content",
                subtitle = "Tap the flag on any AI reply in chat to report offensive, harmful, or inaccurate responses — without leaving the app."
            )
        }

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            androidx.compose.material3.Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Sign out", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun isPremiumTier(tier: String): Boolean =
    tier == SubscriptionTier.BASIC.name || tier == SubscriptionTier.PRO.name

private fun subscriptionDescription(tier: String): String = when (tier) {
    SubscriptionTier.BASIC.name,
    SubscriptionTier.PRO.name -> "Premium AI — deeper tutoring, full study packs, better code help"
    else -> "Standard tutoring, study packs, and code help"
}
