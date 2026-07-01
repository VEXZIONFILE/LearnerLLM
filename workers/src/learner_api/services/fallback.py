from learner_api.services.model_registry import ModelRoute


def offline_fallback(route: ModelRoute) -> str:
    if "laguna" in route.model_id.lower():
        return (
            "I'm in Code Help mode but can't reach the API right now.\n\n"
            "Share the specific function or error message you're stuck on, "
            "and we'll debug it together in small steps."
        )
    if "nemotron" in route.model_id.lower():
        return (
            "## Summary\n"
            "Study mode is offline — the server could not reach the AI provider.\n\n"
            "## Key Concepts\n"
            "- Review your class notes on this topic\n"
            "- Identify vocabulary you don't know yet\n\n"
            "## Flashcards\n"
            "Q: What is the main idea of your topic?\n"
            "A: (Write your answer from your notes)"
        )
    return (
        "I'm here to help you think through this step by step.\n\n"
        "Let's start with what you already know about the problem. Can you tell me:\n"
        "1. What is the problem asking you to find?\n"
        "2. What information have you been given?\n"
        "3. What approach might you try first?\n\n"
        "Remember — I'm not going to give you the answer. My job is to help you discover it yourself!"
    )
