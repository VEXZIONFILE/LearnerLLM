package com.learner.lm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun observeSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)
}

@Dao
interface StudyTopicDao {
    @Query("SELECT * FROM study_topics ORDER BY lastStudiedAt DESC")
    fun observeTopics(): Flow<List<StudyTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(topic: StudyTopicEntity)

    @Query("SELECT * FROM study_topics WHERE strengthScore < :threshold ORDER BY strengthScore ASC")
    suspend fun getWeakTopics(threshold: Float = 0.5f): List<StudyTopicEntity>
}

@Dao
interface LearningStreakDao {
    @Query("SELECT * FROM learning_streaks WHERE id = 1")
    fun observeStreak(): Flow<LearningStreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: LearningStreakEntity)
}

@Dao
interface PracticeProblemDao {
    @Query("SELECT * FROM practice_problems WHERE topicId = :topicId ORDER BY createdAt DESC")
    fun observeByTopic(topicId: Long): Flow<List<PracticeProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(problem: PracticeProblemEntity)
}

@Dao
interface CustomSubjectDao {
    @Query("SELECT * FROM custom_subjects ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CustomSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: CustomSubjectEntity): Long

    @Query("DELETE FROM custom_subjects WHERE id = :id")
    suspend fun delete(id: Long)
}
