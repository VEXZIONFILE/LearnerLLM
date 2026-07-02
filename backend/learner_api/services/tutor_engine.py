from dataclasses import dataclass
import re

from learner_api.schemas import AppMode, HintLevel
from learner_api.services.model_registry import ModelRoute, resolve_model
from learner_api.services.openrouter import OpenRouterClient
from learner_api.services.prompt_builder import PromptBuilder, TutorContext, effective_learning_mode
from learner_api.services.subject_classifier import SubjectClassifier


@dataclass
class TutorResponse:
    message: str
    hint_level: HintLevel
    subject_key: str
    subject_display_name: str
    model_label: str
    detected_mistake: str | None = None
    encourages_attempt: bool = True


class TutorEngine:
    def __init__(
        self,
        openrouter: OpenRouterClient,
        prompt_builder: PromptBuilder | None = None,
        subject_classifier: SubjectClassifier | None = None,
    ) -> None:
        self.openrouter = openrouter
        self.prompt_builder = prompt_builder or PromptBuilder()
        self.subject_classifier = subject_classifier or SubjectClassifier()

    async def respond(self, context: TutorContext, subscription_tier: str) -> TutorResponse:
        subject_key, subject_display, subject_category = self._resolve_subject(context)
        enriched = TutorContext(
            grade_level=context.grade_level,
            subject_key=subject_key,
            subject_display_name=subject_display,
            subject_category=subject_category,
            app_mode=context.app_mode,
            subscription_tier=subscription_tier,
            hint_level=context.hint_level,
            student_message=context.student_message,
            conversation_history=context.conversation_history,
            scanned_text=context.scanned_text,
            free_model_variant=context.free_model_variant,
        )
        route = resolve_model(
            enriched.app_mode,
            subscription_tier,
            enriched.free_model_variant,
        )
        system_prompt = self.prompt_builder.build_system_prompt(enriched)
        user_prompt = self.prompt_builder.build_user_prompt(enriched)

        raw_message = await self.openrouter.chat_completion(
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            route=route,
        )
        sanitized = self._sanitize_response(
            raw_message,
            effective_learning_mode(enriched.app_mode, enriched.free_model_variant),
        )
        learning_mode = effective_learning_mode(enriched.app_mode, enriched.free_model_variant)
        next_hint = (
            self._determine_next_hint_level(enriched, sanitized)
            if learning_mode == AppMode.TUTOR
            else enriched.hint_level
        )

        return TutorResponse(
            message=sanitized,
            hint_level=next_hint,
            subject_key=subject_key,
            subject_display_name=subject_display,
            model_label=route.display_name,
            detected_mistake=self._detect_mistake_seeking(enriched),
            encourages_attempt=not self._contains_direct_answer(sanitized),
        )

    def _resolve_subject(self, context: TutorContext) -> tuple[str, str, str | None]:
        if context.subject_key.startswith("custom:"):
            return context.subject_key, context.subject_display_name, context.subject_category
        builtin = context.subject_key.removeprefix("builtin:")
        learning_mode = effective_learning_mode(context.app_mode, context.free_model_variant)
        if builtin != "GENERAL" or learning_mode == AppMode.CODE:
            return context.subject_key, context.subject_display_name, None
        text = " ".join(filter(None, [context.student_message, context.scanned_text]))
        classified = self.subject_classifier.classify(text)
        return f"builtin:{classified.value}", classified.value.title(), None

    def _determine_next_hint_level(self, context: TutorContext, response: str) -> HintLevel:
        student_attempted = len(context.student_message) > 20 and not self._is_answer_seeking(
            context.student_message
        )
        if student_attempted and context.hint_level == HintLevel.NEAR_SOLUTION:
            return HintLevel.STUDENT_ATTEMPT_REQUIRED
        if self._is_answer_seeking(context.student_message):
            return HintLevel(min(context.hint_level.value + 1, HintLevel.STUDENT_ATTEMPT_REQUIRED.value))
        return context.hint_level

    @staticmethod
    def _is_answer_seeking(message: str) -> bool:
        lower = message.lower()
        phrases = (
            "what is the answer",
            "just tell me",
            "solve this for me",
            "give me the answer",
            "write the whole",
            "build the entire app",
        )
        return any(phrase in lower for phrase in phrases)

    def _detect_mistake_seeking(self, context: TutorContext) -> str | None:
        if not self._is_answer_seeking(context.student_message):
            return None
        learning_mode = effective_learning_mode(context.app_mode, context.free_model_variant)
        if learning_mode == AppMode.TUTOR:
            return "It looks like you're asking for a direct answer. Let's work through this step by step instead."
        if learning_mode == AppMode.CODE:
            return "I can't build full apps or projects — let's focus on one function or bug at a time."
        return None

    @staticmethod
    def _contains_direct_answer(response: str) -> bool:
        lower = response.lower()
        return "the answer is" in lower or "the solution is" in lower

    @staticmethod
    def _sanitize_response(response: str, mode: AppMode) -> str:
        if mode != AppMode.TUTOR:
            return response.strip()
        sanitized = response
        for pattern in (
            re.compile(r"the answer is\s+.+", re.IGNORECASE),
            re.compile(r"the solution is\s+.+", re.IGNORECASE),
        ):
            sanitized = pattern.sub(
                "Let's keep working on this together — what's your next step?",
                sanitized,
            )
        return sanitized.strip()
