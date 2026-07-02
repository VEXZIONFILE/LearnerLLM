package com.learner.lm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.SubjectCategory
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing

@Composable
fun SubjectPicker(
    selectedSubject: StudySubject,
    customSubjects: List<StudySubject.Custom>,
    onSubjectSelected: (StudySubject) -> Unit,
    onAddCustomSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StudySubject.builtinSubjects.forEach { builtin ->
                SubjectChip(
                    label = builtin.displayName,
                    selected = selectedSubject.storageKey == builtin.storageKey,
                    onClick = { onSubjectSelected(builtin) }
                )
            }
            customSubjects.forEach { custom ->
                SubjectChip(
                    label = "${custom.emoji} ${custom.displayName}",
                    selected = selectedSubject.storageKey == custom.storageKey,
                    onClick = { onSubjectSelected(custom) }
                )
            }
            SubjectChip(
                label = "Add",
                selected = false,
                onClick = onAddCustomSubject,
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add custom subject",
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppRadii.pill),
        color = if (selected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AddCustomSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: SubjectCategory) -> Unit,
    error: String? = null
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SubjectCategory.CLASS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadii.lg),
        title = {
            Text("Add subject", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Create a subject for your class, club, or project.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject name") },
                    placeholder = { Text("e.g. Robotics Club") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadii.md)
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubjectCategory.entries.forEach { category ->
                        SubjectChip(
                            label = "${category.emoji} ${category.label}",
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedCategory) },
                enabled = name.isNotBlank()
            ) {
                Text("Add", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
