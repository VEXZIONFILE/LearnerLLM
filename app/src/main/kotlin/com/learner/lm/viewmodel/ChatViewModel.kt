package com.learner.lm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.LearnerLMApplication
import com.learner.lm.ai.HintLevel
import com.learner.lm.ai.Subject
import com.learner.lm.ai.TutorContext
import com.learner.lm.ai.TutorEngine
import com.learner.lm.database.ChatMessageEntity
import com.learner.lm.repository.AiRepository
import com.learner.lm.repository.ChatMessage
import com.learner.lm.repository.NetworkModule
import com.learner.lm.repository.TutorRepository
import com.learner.lm.utils.GradeLevelValidator
import com.learner.lm.utils.SessionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val gradeLevel: Int = 8,
    val subject: Subject = Subject.GENERAL,
    val hintLevel: HintLevel = HintLevel.GENTLE_NUDGE,
    val scannedText: String? = null,
    val error: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LearnerLMApplication
    private val database = app.database
    private val sessionId = SessionUtils.createSessionId()

    private val tutorRepository = TutorRepository(
        chatMessageDao = database.chatMessageDao(),
        studyTopicDao = database.studyTopicDao(),
        learningStreakDao = database.learningStreakDao()
    )

    private val tutorEngine = TutorEngine(
        aiRepository = AiRepository(apiService = NetworkModule.createAiApiService())
    )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tutorRepository.observeMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun setGradeLevel(grade: Int) {
        _uiState.update { it.copy(gradeLevel = GradeLevelValidator.clamp(grade)) }
    }

    fun setSubject(subject: Subject) {
        _uiState.update { it.copy(subject = subject) }
    }

    fun setScannedText(text: String?) {
        _uiState.update { it.copy(scannedText = text) }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            tutorRepository.saveMessage(
                sessionId,
                ChatMessage(role = "student", content = content, subject = state.subject, hintLevel = state.hintLevel)
            )

            try {
                val history = state.messages.map { entity ->
                    entity.role to entity.content
                }
                val response = tutorEngine.respond(
                    TutorContext(
                        gradeLevel = state.gradeLevel,
                        subject = state.subject,
                        hintLevel = state.hintLevel,
                        studentMessage = content,
                        conversationHistory = history,
                        scannedText = state.scannedText
                    )
                )

                tutorRepository.saveMessage(
                    sessionId,
                    ChatMessage(
                        role = "tutor",
                        content = response.message,
                        subject = response.subject,
                        hintLevel = response.hintLevel
                    )
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hintLevel = response.hintLevel,
                        subject = response.subject
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
