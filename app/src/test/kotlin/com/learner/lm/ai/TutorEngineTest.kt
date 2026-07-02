package com.learner.lm.ai

import com.learner.lm.billing.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigTest {
    @Test
    fun `tutor model uses gpt-oss-120b`() {
        assertEquals("openai/gpt-oss-120b", AiConfig.TUTOR_MODEL_ID)
    }

    @Test
    fun `study model uses nemotron 3 super`() {
        assertEquals("nvidia/nemotron-3-super-120b-a12b", AiConfig.STUDY_MODEL_ID)
    }

    @Test
    fun `code model uses laguna m1`() {
        assertEquals("poolside/laguna-m.1", AiConfig.CODE_MODEL_ID)
    }

    @Test
    fun `free models use openrouter free variants`() {
        assertEquals("openai/gpt-oss-120b:free", AiConfig.FREE_TUTOR_MODEL_ID)
        assertEquals("nvidia/nemotron-3-super-120b-a12b:free", AiConfig.FREE_STUDY_MODEL_ID)
        assertEquals("poolside/laguna-m.1:free", AiConfig.FREE_CODE_MODEL_ID)
    }
}

class ModelRegistryTest {
    @Test
    fun `free tier tutor mode routes to selected free model`() {
        val route = ModelRegistry.resolve(AppMode.TUTOR, SubscriptionTier.FREE.name, FreeModelVariant.TUTOR)
        assertEquals(ModelRegistry.FREE_TUTOR_MODEL, route.modelId)
        assertEquals("GPT-OSS", route.displayName)
    }

    @Test
    fun `free tier study mode can use any free model variant`() {
        val route = ModelRegistry.resolve(AppMode.STUDY, SubscriptionTier.FREE.name, FreeModelVariant.CODE)
        assertEquals(ModelRegistry.FREE_CODE_MODEL, route.modelId)
        assertEquals("Laguna", route.displayName)
    }

    @Test
    fun `free tier code mode defaults to laguna free model`() {
        val route = ModelRegistry.resolve(AppMode.CODE, SubscriptionTier.FREE.name, FreeModelVariant.CODE)
        assertEquals(ModelRegistry.FREE_CODE_MODEL, route.modelId)
    }

    @Test
    fun `premium tier increases token limits`() {
        val free = ModelRegistry.resolve(AppMode.STUDY, SubscriptionTier.FREE.name, FreeModelVariant.STUDY)
        val premium = ModelRegistry.resolve(AppMode.STUDY, SubscriptionTier.BASIC.name)
        assertTrue(premium.maxTokens > free.maxTokens)
    }

    @Test
    fun `display label uses model name for free tier`() {
        assertEquals(
            "GPT-OSS",
            ModelRegistry.displayLabel(AppMode.TUTOR, SubscriptionTier.FREE.name, FreeModelVariant.TUTOR)
        )
        assertEquals(
            "Nemotron",
            ModelRegistry.displayLabel(AppMode.STUDY, SubscriptionTier.FREE.name, FreeModelVariant.STUDY)
        )
        assertEquals("Learner Code", ModelRegistry.displayLabel(AppMode.CODE, SubscriptionTier.BASIC.name))
    }

    @Test
    fun `paid tier still routes by mode`() {
        assertEquals(
            ModelRegistry.TUTOR_MODEL,
            ModelRegistry.resolve(AppMode.TUTOR, SubscriptionTier.BASIC.name, FreeModelVariant.CODE).modelId
        )
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
    fun `tutor system prompt enforces no-answer policy`() {
        val prompt = builder.buildSystemPrompt(
            TutorContext(
                gradeLevel = 8,
                subject = StudySubject.Builtin(Subject.MATH),
                appMode = AppMode.TUTOR,
                studentMessage = "help"
            )
        )
        assertTrue(prompt.contains("NEVER give final answers"))
        assertTrue(prompt.contains("Socratic"))
    }

    @Test
    fun `study system prompt requires structured sections`() {
        val prompt = builder.buildSystemPrompt(
            TutorContext(
                gradeLevel = 9,
                subject = StudySubject.Builtin(Subject.SCIENCE),
                appMode = AppMode.STUDY,
                studentMessage = "photosynthesis"
            )
        )
        assertTrue(prompt.contains("## Summary"))
        assertTrue(prompt.contains("Flashcards"))
    }

    @Test
    fun `code system prompt forbids full apps`() {
        val prompt = builder.buildSystemPrompt(
            TutorContext(
                gradeLevel = 10,
                subject = StudySubject.Builtin(Subject.GENERAL),
                appMode = AppMode.CODE,
                studentMessage = "build my app"
            )
        )
        assertTrue(prompt.contains("NEVER generate full applications"))
    }

    @Test
    fun `user prompt includes hint level for tutor mode`() {
        val prompt = builder.buildUserPrompt(
            TutorContext(
                gradeLevel = 7,
                subject = StudySubject.Builtin(Subject.MATH),
                appMode = AppMode.TUTOR,
                hintLevel = HintLevel.GENTLE_NUDGE,
                studentMessage = "How do I solve 2x + 4 = 10?"
            )
        )
        assertTrue(prompt.contains("Hint level: 1"))
        assertTrue(prompt.contains("How do I solve 2x + 4 = 10?"))
    }
}

class SubscriptionCapabilitiesTest {
    @Test
    fun `free tier has limited study sections`() {
        val caps = SubscriptionCapabilities.forTier(SubscriptionTier.FREE.name)
        assertEquals(SubscriptionCapabilities.StudySectionDepth.BASIC, caps.studySections)
    }

    @Test
    fun `premium tier unlocks full study packs`() {
        val caps = SubscriptionCapabilities.forTier(SubscriptionTier.BASIC.name)
        assertTrue(caps.isPremium)
        assertEquals(SubscriptionCapabilities.StudySectionDepth.FULL, caps.studySections)
        assertNull(caps.dailyHomeworkScans)
    }

    @Test
    fun `free tier limits homework scans`() {
        val caps = SubscriptionCapabilities.forTier(SubscriptionTier.FREE.name)
        assertEquals(3, caps.dailyHomeworkScans)
    }
}
