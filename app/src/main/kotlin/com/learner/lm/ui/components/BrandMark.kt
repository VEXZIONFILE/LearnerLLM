package com.learner.lm.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.learner.lm.ui.theme.NotebookColors

/**
 * Compact brand lockup — small icon (launcher proportions) + optional wordmark text.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    showWordmark: Boolean = true,
    onSurface: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (onSurface) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            tonalElevation = 0.dp
        ) {
            LearnerLogo(
                showWordmark = false,
                modifier = Modifier
                    .padding(3.dp)
                    .size(iconSize - 6.dp)
            )
        }
        if (showWordmark) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LearnerLM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (onSurface) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    NotebookColors.GoogleBlue
                }
            )
        }
    }
}
