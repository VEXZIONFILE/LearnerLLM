package com.learner.lm.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.learner.lm.ui.theme.AppColors

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
        LearnerLogo(
            showWordmark = false,
            modifier = Modifier.size(iconSize)
        )
        if (showWordmark) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "LearnerLM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (onSurface) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    AppColors.Accent
                }
            )
        }
    }
}
