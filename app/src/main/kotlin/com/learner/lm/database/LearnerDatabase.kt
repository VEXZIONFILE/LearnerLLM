package com.learner.lm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        StudyTopicEntity::class,
        LearningStreakEntity::class,
        PracticeProblemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LearnerDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studyTopicDao(): StudyTopicDao
    abstract fun learningStreakDao(): LearningStreakDao
    abstract fun practiceProblemDao(): PracticeProblemDao

    companion object {
        @Volatile
        private var instance: LearnerDatabase? = null

        fun getInstance(context: Context): LearnerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LearnerDatabase::class.java,
                    "learner_lm.db"
                ).build().also { instance = it }
            }
    }
}
