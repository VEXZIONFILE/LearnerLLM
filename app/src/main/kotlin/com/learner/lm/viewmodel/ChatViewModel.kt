package com.learner.lm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.LearnerLMApplication
import com.learner.lm.ai.AiReportReason
import com.learner.lm.ai.AppMode
import com.learner.lm.ai.HintLevel
import com.learner.lm.ai.ModelRegistry
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.Subject
import com.learner.lm.ai.SubjectCategory
import com.learner.lm.ai.TutorContext
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.database.ChatMessageEntity
import com.learner.lm.repository.ChatMessage
import com.learner.lm.repository.LearnerApiConfig
import com.learner.lm.repository.LearnerChatRepository
import com.learner.lm.repository.SubjectRepository
import com.learner.lm.repository.TutorRepository
import com.learner.lm.utils.GradeLevelValidator
import com.learner.lm.utils.SessionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportTarget(
    val messageId: Long,
    val content: String
)

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val gradeLevel: Int = 8,
    val selectedMode: AppMode = AppMode.TUTOR,
    val subscriptionTier: String = SubscriptionTier.FREE.name,
    val selectedSubject: StudySubject = StudySubject.Builtin(Subject.GENERAL),
    val customSubjects: List<StudySubject.Custom> = emptyList(),
    val hintLevel: HintLevel = HintLevel.GENTLE_NUDGE,
    val scannedText: String? = null,
    val error: String? = null,
    val showAddSubjectDialog: Boolean = false,
    val reportTarget: ReportTarget? = null,
    val isSubmittingReport: Boolean = false,
    val reportConfirmation: String? = null,
    val reportError: String? = null
) {
    val activeModelLabel: String
        get() = ModelRegistry.displayLabel(selectedMode, subscriptionTier)
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LearnerLMApplication
    private val database = app.database
    private val sessionId = SessionUtils.createSessionId()

    private val tutorRepository = TutorRepository(
        chatMessageDao = database.chatMessageDao(),
        studyTopicDao = database.studyTopicDao(),
        learningStreakDao = database.learningStreakDao()
    )

    private val subjectRepository = SubjectRepository(
        customSubjectDao = database.customSubjectDao()
    )

    private val learnerChatRepository: LearnerChatRepository? by lazy {
        if (LearnerApiConfig.isConfigured) {
            try {
                LearnerChatRepository()
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tutorRepository.observeMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        viewModelScope.launch {
            subjectRepository.observeCustomSubjects().collect { customSubjects ->
                _uiState.update { state ->
                    val selected = when (val current = state.selectedSubject) {
                        is StudySubject.Custom ->
                            customSubjects.find { it.id == current.id } ?: current
                        else -> state.selectedSubject
                    }
                    state.copy(customSubjects = customSubjects, selectedSubject = selected)
                }
            }
        }
    }

    fun setGradeLevel(grade: Int) {
        _uiState.update { it.copy(gradeLevel = GradeLevelValidator.clamp(grade)) }
    }

    fun setSubscriptionTier(tier: String) {
        _uiState.update { it.copy(subscriptionTier = tier) }
    }

    fun selectMode(mode: AppMode) {
        _uiState.update { it.copy(selectedMode = mode, error = null) }
    }

    fun selectSubject(subject: StudySubject) {
        _uiState.update { it.copy(selectedSubject = subject, error = null) }
    }

    fun setScannedText(text: String?) {
        _uiState.update { it.copy(scannedText = text) }
    }

    fun showAddSubjectDialog() {
        _uiState.update { it.copy(showAddSubjectDialog = true) }
    }

    fun dismissAddSubjectDialog() {
        _uiState.update { it.copy(showAddSubjectDialog = false, error = null) }
    }

    fun addCustomSubject(name: String, category: SubjectCategory) {
        viewModelScope.launch {
            try {
                val subject = subjectRepository.addCustomSubject(name, category)
                _uiState.update {
                    it.copy(
                        selectedSubject = subject,
                        showAddSubjectDialog = false,
                        error = null
                    )
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCustomSubject(id: Long) {
        viewModelScope.launch {
            subjectRepository.deleteCustomSubject(id)
            _uiState.update { state ->
                val resetSelection = if (
                    state.selectedSubject is StudySubject.Custom &&
                    state.selectedSubject.id == id
                ) {
                    StudySubject.Builtin(Subject.GENERAL)
                } else {
                    state.selectedSubject
                }
                state.copy(selectedSubject = resetSelection)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            tutorRepository.saveMessage(
                sessionId,
                ChatMessage(
                    role = "student",
                    content = content,
                    subject = state.selectedSubject,
                    hintLevel = state.hintLevel
                )
            )

            try {
                val history = state.messages.map { entity ->
                    entity.role to entity.content
                }
                val context = TutorContext(
                    gradeLevel = state.gradeLevel,
                    subject = state.selectedSubject,
                    appMode = state.selectedMode,
                    subscriptionTier = state.subscriptionTier,
                    hintLevel = state.hintLevel,
                    studentMessage = content,
                    conversationHistory = history,
                    scannedText = state.scannedText
                )
                val chatRepository = learnerChatRepository
                    ?: throw IllegalStateException(
                        "Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."
                    )
                val response = chatRepository.sendMessage(sessionId, context)

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
                        hintLevel = if (state.selectedMode == AppMode.TUTOR) {
                            response.hintLevel
                        } else {
                            HintLevel.GENTLE_NUDGE
                        },
                        selectedSubject = response.subject
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun openReportDialog(messageId: Long, content: String) {
        _uiState.update {
            it.copy(
                reportTarget = ReportTarget(messageId = messageId, content = content),
                reportError = null
            )
        }
    }

    fun dismissReportDialog() {
        if (_uiState.value.isSubmittingReport) return
        _uiState.update { it.copy(reportTarget = null, reportError = null) }
    }

    fun clearReportConfirmation() {
        _uiState.update { it.copy(reportConfirmation = null) }
    }

    fun clearReportError() {
        _uiState.update { it.copy(reportError = null) }
    }

    fun submitReport(reason: AiReportReason, details: String?) {
        val target = _uiState.value.reportTarget ?: return
        if (_uiState.value.isSubmittingReport) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReport = true, reportError = null) }
            try {
                val chatRepository = learnerChatRepository
                    ?: throw IllegalStateException(
                        "Learner API not configured. Set LEARNER_API_BASE_URL in local.properties."
                    )
                chatRepository.reportContent(
                    sessionId = sessionId,
                    messageId = target.messageId,
                    content = target.content,
                    reason = reason,
                    details = details,
                    appMode = _uiState.value.selectedMode
                )
                _uiState.update {
                    it.copy(
                        reportTarget = null,
                        isSubmittingReport = false,
                        reportConfirmation = "Thanks — your report was submitted. We'll review it shortly."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmittingReport = false,
                        reportError = e.message ?: "Could not submit report. Try again."
                    )
                }
            }
        }
    }
}
