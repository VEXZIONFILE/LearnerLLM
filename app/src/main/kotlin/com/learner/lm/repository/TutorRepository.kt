package com.learner.lm.repository

import com.learner.lm.ai.HintLevel
import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.Subject
import com.learner.lm.database.ChatMessageDao
import com.learner.lm.database.ChatMessageEntity
import com.learner.lm.database.LearningStreakDao
import com.learner.lm.database.LearningStreakEntity
import com.learner.lm.database.StudyTopicDao
import com.learner.lm.database.StudyTopicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ChatMessage(
    val id: Long = 0,
    val role: String,
    val content: String,
    val subject: StudySubject = StudySubject.Builtin(Subject.GENERAL),
    val hintLevel: HintLevel = HintLevel.GENTLE_NUDGE,
    val timestamp: Long = System.currentTimeMillis()
)

class TutorRepository(
    private val chatMessageDao: ChatMessageDao,
    private val studyTopicDao: StudyTopicDao,
    private val learningStreakDao: LearningStreakDao
) {
    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.observeSession(sessionId)

    suspend fun saveMessage(sessionId: String, message: ChatMessage) {
        chatMessageDao.insert(
            ChatMessageEntity(
                sessionId = sessionId,
                role = message.role,
                content = message.content,
                subject = message.subject.storageKey,
                hintLevel = message.hintLevel.level
            )
        )
        recordStudyActivity(message.subject)
        updateStreak()
    }

    fun observeTopics(): Flow<List<StudyTopicEntity>> = studyTopicDao.observeTopics()

    fun observeStreak(): Flow<LearningStreakEntity?> = learningStreakDao.observeStreak()

    suspend fun getWeakTopics(): List<StudyTopicEntity> = studyTopicDao.getWeakTopics()

    private suspend fun recordStudyActivity(subject: StudySubject) {
        studyTopicDao.upsert(
            StudyTopicEntity(
                name = subject.displayName,
                subject = subject.storageKey,
                strengthScore = 0.4f
            )
        )
    }

    private suspend fun updateStreak() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val current = learningStreakDao.observeStreak().first() ?: LearningStreakEntity()

        val newStreak = when (current.lastActiveDate) {
            today -> current.currentStreak
            LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE) -> current.currentStreak + 1
            else -> 1
        }

        learningStreakDao.upsert(
            current.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, current.longestStreak),
                lastActiveDate = today
            )
        )
    }
}
