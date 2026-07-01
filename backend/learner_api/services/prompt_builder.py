from dataclasses import dataclass

from learner_api.schemas import AppMode, BuiltinSubject, HintLevel, SubjectCategory
from learner_api.services.model_registry import is_premium_tier, is_pro_tier


@dataclass
class TutorContext:
    grade_level: int
    subject_key: str
    subject_display_name: str
    subject_category: str | None
    app_mode: AppMode
    subscription_tier: str
    hint_level: HintLevel
    student_message: str
    conversation_history: list[tuple[str, str]]
    scanned_text: str | None = None


BUILTIN_LABELS = {
    BuiltinSubject.MATH: "Math",
    BuiltinSubject.SCIENCE: "Science",
    BuiltinSubject.ENGLISH: "English",
    BuiltinSubject.HISTORY: "History",
    BuiltinSubject.GEOGRAPHY: "Geography",
    BuiltinSubject.GENERAL: "General",
}

CATEGORY_LABELS = {
    SubjectCategory.CLASS: "Class",
    SubjectCategory.AFTER_SCHOOL: "After School",
    SubjectCategory.PROJECT: "Project",
    SubjectCategory.CLUB: "Club / Activity",
    SubjectCategory.EXAM_PREP: "Exam Prep",
    SubjectCategory.OTHER: "Other",
}


class PromptBuilder:
    def build_system_prompt(self, context: TutorContext) -> str:
        premium = is_premium_tier(context.subscription_tier)
        if context.app_mode == AppMode.TUTOR:
            return self._build_tutor_system_prompt(context, premium)
        if context.app_mode == AppMode.STUDY:
            return self._build_study_system_prompt(context, premium)
        return self._build_code_system_prompt(context, premium)

    def build_user_prompt(self, context: TutorContext) -> str:
        history_limit = 16 if is_pro_tier(context.subscription_tier) else (
            12 if is_premium_tier(context.subscription_tier) else 8
        )
        history = "\n".join(
            f"{role}: {content}" for role, content in context.conversation_history[-history_limit:]
        )
        scanned = f"\n\nScanned material:\n{context.scanned_text}" if context.scanned_text else ""
        subject_line = f"Subject: {context.subject_display_name}"
        if context.subject_category:
            subject_line += f" ({context.subject_category})"
        tier_note = when_tier_note(context.subscription_tier)

        if context.app_mode == AppMode.TUTOR:
            return (
                f"Grade level: {context.grade_level}\n"
                f"{subject_line}\n"
                f"Mode: Tutor (Socratic, step-by-step)\n"
                f"Hint level: {context.hint_level.value}\n"
                f"{tier_note}{scanned}\n\n"
                f"Recent conversation:\n{history}\n\n"
                f"Student message: {context.student_message}\n\n"
                "Guide the student step-by-step. Ask a question back. Do not give the final answer."
            )
        if context.app_mode == AppMode.STUDY:
            return (
                f"Grade level: {context.grade_level}\n"
                f"{subject_line}\n"
                f"Mode: Study pack generator\n"
                f"{tier_note}{scanned}\n\n"
                f"Topic / request: {context.student_message}\n\n"
                f"Produce structured study materials for this topic at grade {context.grade_level} level."
            )
        code_lines = code_line_limit(context.subscription_tier)
        return (
            f"Grade level: {context.grade_level}\n"
            f"{subject_line}\n"
            f"Mode: Code help (debug & teach only)\n"
            f"{tier_note}{scanned}\n\n"
            f"Recent conversation:\n{history}\n\n"
            f"Student code question: {context.student_message}\n\n"
            f"Explain and debug in small teachable pieces. Max ~{code_lines} lines of suggested code."
        )

    def _build_tutor_system_prompt(self, context: TutorContext, premium: bool) -> str:
        examples = tutor_example_count(context.subscription_tier)
        return (
            "You are Learner LM Tutor Mode — powered by gpt-oss-120b for grades 6–12.\n\n"
            "ROLE: Primary Socratic tutor. Guide learning step-by-step. Never be a homework solver.\n\n"
            "NON-NEGOTIABLE RULES:\n"
            "- NEVER give final answers without prior guided steps\n"
            "- NEVER say \"the answer is...\" or complete the student's work\n"
            "- ALWAYS ask questions back to check understanding\n"
            f"- ALWAYS adapt difficulty to grade {context.grade_level}\n"
            "- Break problems into small steps the student can attempt\n\n"
            f"HINT LADDER (level {context.hint_level.value}):\n"
            "- Level 1: Gentle directional nudge\n"
            "- Level 2: Deeper concept explanation\n"
            "- Level 3: Near-solution guidance (student must finish)\n"
            "- Level 4: Require a student attempt before more help\n\n"
            f"{self._grade_guidance(context.grade_level)}\n\n"
            f"{self._subject_guidance(context)}\n\n"
            f"Provide up to {examples} worked examples of reasoning (not final answers).\n\n"
            "Celebrate reasoning, not just results. When you spot an error, guide discovery — don't correct outright."
        )

    def _build_study_system_prompt(self, context: TutorContext, premium: bool) -> str:
        pro = is_pro_tier(context.subscription_tier)
        if pro:
            sections = (
                "REQUIRED OUTPUT SECTIONS (use these exact headings):\n"
                "## Summary\n## Key Concepts\n## Flashcards\n(Format each as Q: ... / A: ...)\n"
                "## Quiz Questions\n(Number each question; include answer key at the end)\n"
                "## Practice Problems\n(3–5 problems with hints — no full solutions)"
            )
        elif premium:
            sections = (
                "REQUIRED OUTPUT SECTIONS (use these exact headings):\n"
                "## Summary\n## Key Concepts\n## Flashcards\n(Format each as Q: ... / A: ...)\n"
                "## Quiz Questions\n(Number each question; include answer key at the end)"
            )
        else:
            sections = (
                "REQUIRED OUTPUT SECTIONS (use these exact headings):\n"
                "## Summary\n## Key Concepts\n## Flashcards\n(Format each as Q: ... / A: ... — include at least 3 cards)"
            )
        return (
            "You are Learner LM Study Mode — powered by NVIDIA Nemotron 3 Super.\n"
            f"Behave like a NotebookLM-style study generator for grade {context.grade_level} students.\n\n"
            "ROLE: Convert topics into structured, study-ready materials.\n\n"
            "RULES:\n"
            "- Always use clear markdown headings\n"
            f"- Adapt vocabulary to grade {context.grade_level}\n"
            "- Flashcards must be Q/A format\n"
            "- Quiz questions must test understanding, not trivia\n"
            "- Never do homework for the student — create materials to learn FROM\n\n"
            f"{sections}"
        )

    def _build_code_system_prompt(self, context: TutorContext, premium: bool) -> str:
        line_limit = code_line_limit(context.subscription_tier)
        depth = (
            "Give exhaustive line-by-line explanations, multiple debugging strategies, and edge-case notes."
            if is_pro_tier(context.subscription_tier)
            else (
                "Give detailed line-by-line explanations and multiple debugging strategies."
                if premium
                else "Give concise explanations focused on the immediate bug or concept."
            )
        )
        return (
            "You are Learner LM Code Help Mode — powered by Poolside Laguna M.1.\n"
            f"Programming tutor for grade {context.grade_level} students learning to code.\n\n"
            "ROLE: Debug and explain code step-by-step. Teach programming — do not build projects for students.\n\n"
            "NON-NEGOTIABLE RULES:\n"
            "- NEVER generate full applications, full systems, or complete projects\n"
            "- ONLY help with: single functions, small components, bug fixes, syntax, logic errors\n"
            f"- Maximum ~{line_limit} lines of suggested code per response\n"
            "- Explain WHY each fix works\n"
            "- If asked to build an entire app, refuse politely and offer to break it into small learning steps\n\n"
            f"{depth}\n\n"
            "Use code blocks with language tags. Ask the student what they expect the code to do before fixing."
        )

    def _grade_guidance(self, grade: int) -> str:
        if 6 <= grade <= 8:
            return (
                "Grades 6–8: Use simple language, short sentences, heavy step-by-step guidance. "
                "Define every new term. Check understanding after each step."
            )
        if 9 <= grade <= 10:
            return (
                "Grades 9–10: Moderate explanation depth. Introduce technical terms with brief definitions. "
                "Balance guidance with student independence."
            )
        if 11 <= grade <= 12:
            return (
                "Grades 11–12: Faster reasoning, less hand-holding. Use precise academic language. "
                "Expect more student initiative; probe with harder questions."
            )
        return "Adapt explanations to an appropriate middle or high school level."

    def _subject_guidance(self, context: TutorContext) -> str:
        if context.subject_key.startswith("custom:"):
            return (
                f'Custom subject: "{context.subject_display_name}" ({context.subject_category}). '
                "Tailor tutoring to this context while maintaining Socratic guidance."
            )
        builtin = context.subject_key.removeprefix("builtin:")
        guidance = {
            "MATH": "Focus on reasoning and formula discovery. Never compute final answers.",
            "SCIENCE": "Explain processes, cause-effect relationships, and use analogies.",
            "ENGLISH": "Give grammar and structure hints. Never write complete essays.",
            "HISTORY": "Guide timeline reasoning and contextual analysis.",
            "GEOGRAPHY": "Encourage spatial reasoning and location analysis.",
            "GENERAL": "Break problems into smaller parts and ask guiding questions.",
        }
        return guidance.get(builtin, guidance["GENERAL"])


def when_tier_note(tier: str) -> str:
    if is_pro_tier(tier):
        return "Subscription: Premium — maximum depth, longest responses, richest examples."
    if is_premium_tier(tier):
        return "Subscription: Pro — provide richer examples and more detail."
    return "Subscription: Standard — keep responses concise but correct."


def tutor_example_count(tier: str) -> int:
    if is_pro_tier(tier):
        return 5
    if is_premium_tier(tier):
        return 3
    return 1


def code_line_limit(tier: str) -> int:
    if is_pro_tier(tier):
        return 60
    if is_premium_tier(tier):
        return 40
    return 20
