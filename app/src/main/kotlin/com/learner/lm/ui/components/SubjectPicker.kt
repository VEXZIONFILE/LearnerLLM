package com.learner.lm.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.SubjectCategory

@Composable
fun SubjectPicker(
    selectedSubject: StudySubject,
    customSubjects: List<StudySubject.Custom>,
    onSubjectSelected: (StudySubject) -> Unit,
    onAddCustomSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "What are you working on?",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            FilterChip(
                selected = false,
                onClick = onAddCustomSubject,
                label = { Text("Add yours") },
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = "Add custom subject")
                }
            )
        }
    }
}

@Composable
private fun SubjectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
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
        title = { Text("Add your subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Create a subject for your class, after-school activity, project, or anything else you're learning!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject name") },
                    placeholder = { Text("e.g. Robotics Club, History Class") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text("${category.emoji} ${category.label}") }
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
