package com.learner.lm.ai

import org.junit.Assert.assertEquals
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
        val prompt = builder.buildSystemPrompt(8, Subject.MATH)
        assertTrue(prompt.contains("NEVER provide final answers"))
        assertTrue(prompt.contains("Socratic"))
    }

    @Test
    fun `user prompt includes hint level`() {
        val prompt = builder.buildUserPrompt(
            TutorContext(
                gradeLevel = 7,
                subject = Subject.MATH,
                hintLevel = HintLevel.GENTLE_NUDGE,
                studentMessage = "How do I solve 2x + 4 = 10?"
            )
        )
        assertTrue(prompt.contains("hint level: 1"))
        assertTrue(prompt.contains("How do I solve 2x + 4 = 10?"))
    }
}
