package com.learner.lm.repository

import com.learner.lm.ai.StudySubject
import com.learner.lm.ai.SubjectCategory
import com.learner.lm.database.CustomSubjectDao
import com.learner.lm.database.CustomSubjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubjectRepository(private val customSubjectDao: CustomSubjectDao) {

    fun observeCustomSubjects(): Flow<List<StudySubject.Custom>> =
        customSubjectDao.observeAll().map { entities ->
            entities.map { it.toStudySubject() }
        }

    suspend fun addCustomSubject(
        name: String,
        category: SubjectCategory,
        emoji: String = category.emoji
    ): StudySubject.Custom {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Subject name cannot be empty" }
        require(trimmed.length <= 40) { "Subject name is too long" }

        val id = customSubjectDao.insert(
            CustomSubjectEntity(
                name = trimmed,
                category = category.name,
                emoji = emoji
            )
        )
        return StudySubject.Custom(id = id, name = trimmed, category = category, emoji = emoji)
    }

    suspend fun deleteCustomSubject(id: Long) {
        customSubjectDao.delete(id)
    }

    private fun CustomSubjectEntity.toStudySubject(): StudySubject.Custom {
        val category = SubjectCategory.entries.find { it.name == this.category } ?: SubjectCategory.OTHER
        return StudySubject.Custom(id = id, name = name, category = category, emoji = emoji)
    }
}
