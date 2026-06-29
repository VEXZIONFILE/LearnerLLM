package com.learner.lm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ui.screens.ChatScreen
import com.learner.lm.ui.screens.ProgressScreen
import com.learner.lm.ui.screens.ScannerScreen
import com.learner.lm.viewmodel.ChatViewModel

private enum class LearnerTab(val label: String) {
    CHAT("Chat"),
    SCANNER("Scanner"),
    PROGRESS("Progress")
}

@Composable
fun LearnerApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val chatViewModel: ChatViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                LearnerTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    LearnerTab.CHAT -> Icons.AutoMirrored.Filled.Chat
                                    LearnerTab.SCANNER -> Icons.Default.CameraAlt
                                    LearnerTab.PROGRESS -> Icons.Default.TrendingUp
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (LearnerTab.entries[selectedTab]) {
            LearnerTab.CHAT -> ChatScreen(
                viewModel = chatViewModel,
                modifier = Modifier.padding(padding)
            )
            LearnerTab.SCANNER -> ScannerScreen(
                onTextScanned = { text -> chatViewModel.setScannedText(text) },
                modifier = Modifier.padding(padding)
            )
            LearnerTab.PROGRESS -> ProgressScreen(modifier = Modifier.padding(padding))
        }
    }
}
