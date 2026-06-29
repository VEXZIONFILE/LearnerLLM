package com.learner.lm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String,
    val content: String,
    val subject: String,
    val hintLevel: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String,
    val strengthScore: Float = 0f,
    val lastStudiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_streaks")
data class LearningStreakEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = ""
)

@Entity(tableName = "practice_problems")
data class PracticeProblemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val prompt: String,
    val subject: String,
    val difficulty: Int,
    val createdAt: Long = System.currentTimeMillis()
)
