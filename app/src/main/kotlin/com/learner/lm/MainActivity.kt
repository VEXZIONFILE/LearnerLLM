package com.learner.lm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.learner.lm.ui.LearnerApp
import com.learner.lm.ui.theme.LearnerLMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnerLMTheme {
                LearnerApp()
            }
        }
    }
}
