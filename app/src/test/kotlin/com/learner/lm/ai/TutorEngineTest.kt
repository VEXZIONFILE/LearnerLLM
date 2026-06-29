package com.learner.lm.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigTest {
    @Test
    fun `uses openrouter gpt-oss-120b model id`() {
        assertEquals("openai/gpt-oss-120b", AiConfig.MODEL_ID)
    }

    @Test
    fun `model is displayed as LearnerLM`() {
        assertEquals("LearnerLM", AiConfig.MODEL_DISPLAY_NAME)
    }
}

class StudySubjectTest {
    @Test
    fun `custom subject preserves name and category`() {
        val subject = StudySubject.Custom(
            id = 1,
            name = "Robotics Club",
            category = SubjectCategory.AFTER_SCHOOL
        )
        assertEquals("Robotics Club", subject.displayName)
        assertEquals("custom:1", subject.storageKey)
        assertEquals("After School", subject.categoryLabel)
    }

    @Test
    fun `fromStorageKey restores custom subject`() {
        val custom = StudySubject.Custom(2, "History Class", SubjectCategory.CLASS)
        val restored = StudySubject.fromStorageKey("custom:2", listOf(custom))
        assertEquals(custom, restored)
    }
}

class SubjectClassifierTest {

    private val classifier = SubjectClassifier()

    @Test
    fun `classifies math problems`() {
        assertEquals(Subject.MATH, classifier.classify("Solve this algebra equation for x"))
    }

    @Test
    fun `classifies science questions`() {
        assertEquals(Subject.SCIENCE, classifier.classify("Explain photosynthesis in plant cells"))
    }

    @Test
    fun `defaults to general for ambiguous text`() {
        assertEquals(Subject.GENERAL, classifier.classify("Help me with this"))
    }
}

class PromptBuilderTest {

    private val builder = PromptBuilder()

    @Test
    fun `system prompt enforces no-answer policy`() {
        val prompt = builder.buildSystemPrompt(8, StudySubject.Builtin(Subject.MATH))
        assertTrue(prompt.contains("NEVER provide final answers"))
        assertTrue(prompt.contains("Socratic"))
    }

    @Test
    fun `custom subject prompt includes student context`() {
        val prompt = builder.buildSystemPrompt(
            9,
            StudySubject.Custom(1, "Science Fair Project", SubjectCategory.PROJECT)
        )
        assertTrue(prompt.contains("Science Fair Project"))
        assertTrue(prompt.contains("Project"))
    }

    @Test
    fun `user prompt includes hint level`() {
        val prompt = builder.buildUserPrompt(
            TutorContext(
                gradeLevel = 7,
                subject = StudySubject.Builtin(Subject.MATH),
                hintLevel = HintLevel.GENTLE_NUDGE,
                studentMessage = "How do I solve 2x + 4 = 10?"
            )
        )
        assertTrue(prompt.contains("hint level: 1"))
        assertTrue(prompt.contains("How do I solve 2x + 4 = 10?"))
    }
}
