package com.learner.lm.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.learner.lm.R

@Composable
fun LearnerLogo(
    modifier: Modifier = Modifier,
    showWordmark: Boolean = true,
    contentDescription: String = "LearnerLM"
) {
    Image(
        painter = painterResource(
            if (showWordmark) R.drawable.learnerlm_logo else R.drawable.learnerlm_icon
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
