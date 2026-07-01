package com.learner.lm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.LearnerLMApplication
import com.learner.lm.database.LearningStreakEntity
import com.learner.lm.database.StudyTopicEntity
import com.learner.lm.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val topics: List<StudyTopicEntity> = emptyList(),
    val streak: LearningStreakEntity? = null,
    val weakTopics: List<StudyTopicEntity> = emptyList()
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as LearnerLMApplication).database
    private val tutorRepository = TutorRepository(
        chatMessageDao = database.chatMessageDao(),
        studyTopicDao = database.studyTopicDao(),
        learningStreakDao = database.learningStreakDao()
    )

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tutorRepository.observeTopics().collect { topics ->
                _uiState.update {
                    it.copy(
                        topics = topics,
                        weakTopics = topics
                            .filter { topic -> topic.strengthScore < 0.5f }
                            .sortedBy { topic -> topic.strengthScore }
                    )
                }
            }
        }
        viewModelScope.launch {
            tutorRepository.observeStreak().collect { streak ->
                _uiState.update { it.copy(streak = streak) }
            }
        }
    }
}
