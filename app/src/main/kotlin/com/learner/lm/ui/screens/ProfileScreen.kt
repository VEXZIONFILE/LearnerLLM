package com.learner.lm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
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
import com.learner.lm.ui.theme.learnerHeroBrush

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onGradeLevelChange: (Int) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val darkTheme = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(learnerHeroBrush(darkTheme))
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    name = profile.displayName,
                    photoUrl = profile.photoUrl,
                    size = 96.dp,
                    modifier = Modifier.clip(RoundedCornerShape(48.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                NotebookBadge(
                    text = tierLabel(profile.subscriptionTier),
                    highlighted = profile.subscriptionTier == SubscriptionTier.PRO.name
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    label = "Grade",
                    value = "${profile.gradeLevel}",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    label = "Plan",
                    value = planShortLabel(profile.subscriptionTier),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeader(title = "Learning")
            NotebookCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Grade level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                    )
                    Text(
                        text = "Tutor responses adapt to grade ${profile.gradeLevel}",
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

            SectionHeader(title = "Account")
            NotebookCard {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Star,
                        title = "Subscription",
                        subtitle = subscriptionDescription(profile.subscriptionTier),
                        trailing = "Manage",
                        onClick = onNavigateToSubscription
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    SettingsRow(
                        icon = Icons.Default.Grade,
                        title = "Grade ${profile.gradeLevel}",
                        subtitle = "Adjust how advanced explanations are"
                    )
                }
            }

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Sign out", fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun tierLabel(tier: String): String = when (tier) {
    SubscriptionTier.BASIC.name -> "Learner Basic"
    SubscriptionTier.PRO.name -> "Learner Pro"
    else -> "Free plan"
}

private fun planShortLabel(tier: String): String = when (tier) {
    SubscriptionTier.BASIC.name -> "Basic"
    SubscriptionTier.PRO.name -> "Pro"
    else -> "Free"
}

private fun subscriptionDescription(tier: String): String = when (tier) {
    SubscriptionTier.BASIC.name -> "Unlimited study chat and homework help"
    SubscriptionTier.PRO.name -> "Priority AI, practice generator, and more"
    else -> "Upgrade for unlimited tutoring"
}
