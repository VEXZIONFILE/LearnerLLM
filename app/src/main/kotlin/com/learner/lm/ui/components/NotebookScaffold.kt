package com.learner.lm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learner.lm.auth.UserProfile
import com.learner.lm.ui.navigation.AppDestination
import com.learner.lm.ui.theme.NotebookColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScaffold(
    currentDestination: AppDestination,
    userProfile: UserProfile?,
    onNavigate: (AppDestination) -> Unit,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            if (!showBack) {
                                BrandMark(iconSize = 26.dp, showWordmark = true)
                            } else {
                                Text(
                                    text = currentDestination.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (!showBack) {
                                Text(
                                    text = currentDestination.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (showBack && onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (currentDestination == AppDestination.Profile && userProfile != null) {
                            ProfileAvatar(
                                name = userProfile.displayName,
                                photoUrl = userProfile.photoUrl,
                                size = 32.dp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    thickness = 0.5.dp
                )
            }
        },
        bottomBar = {
            if (currentDestination.showsBottomNav) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        thickness = 0.5.dp
                    )
                    LearnerBottomNav(
                        currentDestination = currentDestination,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

@Composable
private fun LearnerBottomNav(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        AppDestination.bottomNavDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination == destination,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        destination.icon(),
                        contentDescription = destination.shortLabel,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        destination.shortLabel,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = NotebookColors.NotebookChipSelected.copy(alpha = 0.65f)
                )
            )
        }
    }
}

private fun AppDestination.icon(): ImageVector = when (this) {
    AppDestination.Chat -> Icons.AutoMirrored.Filled.Chat
    AppDestination.Scanner -> Icons.Default.CameraAlt
    AppDestination.Progress -> Icons.Default.Insights
    AppDestination.Profile -> Icons.Default.Person
    else -> Icons.AutoMirrored.Filled.Chat
}

@Composable
fun NotebookCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
fun NotebookBadge(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (highlighted) {
            NotebookColors.ProGold.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            1.dp,
            if (highlighted) NotebookColors.ProGold.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (highlighted) NotebookColors.ProGold else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/** NotebookLM-style inset panel for toolbars and chip rows. */
@Composable
fun NotebookPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        tonalElevation = 0.dp
    ) {
        content()
    }
}
