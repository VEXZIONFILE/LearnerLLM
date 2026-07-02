package com.learner.lm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.FreeModelVariant
import com.learner.lm.ai.StudySubject
import com.learner.lm.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsSheet(
    selectedMode: AppMode,
    selectedFreeModel: FreeModelVariant,
    selectedSubject: StudySubject,
    customSubjects: List<StudySubject.Custom>,
    isFreeTier: Boolean,
    messageQuotaLabel: String,
    onModeSelected: (AppMode) -> Unit,
    onFreeModelSelected: (FreeModelVariant) -> Unit,
    onSubjectSelected: (StudySubject) -> Unit,
    onAddCustomSubject: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .padding(bottom = AppSpacing.xl)
        ) {
            Text(
                text = "Chat settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (messageQuotaLabel.isNotBlank()) {
                Text(
                    text = messageQuotaLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = "Learning mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            AppModePicker(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isFreeTier) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = "AI model",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                FreeModelPicker(
                    selectedVariant = selectedFreeModel,
                    onVariantSelected = onFreeModelSelected,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = "Subject",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            SubjectPicker(
                selectedSubject = selectedSubject,
                customSubjects = customSubjects,
                onSubjectSelected = onSubjectSelected,
                onAddCustomSubject = onAddCustomSubject,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
