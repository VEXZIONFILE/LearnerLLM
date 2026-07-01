from learner_api.config import Settings
from learner_api.http_client import post_json
from learner_api.services.fallback import offline_fallback
from learner_api.services.model_registry import ModelRoute


class OpenRouterClient:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    async def chat_completion(
        self,
        system_prompt: str,
        user_prompt: str,
        route: ModelRoute,
    ) -> str:
        api_key = self.settings.openrouter_api_key.strip()
        if not api_key:
            return offline_fallback(route)

        headers = {
            "Authorization": f"Bearer {api_key}",
            "HTTP-Referer": self.settings.openrouter_referer,
            "X-Title": self.settings.openrouter_title,
            "Content-Type": "application/json",
        }
        payload = {
            "model": route.model_id,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": route.temperature,
            "max_tokens": route.max_tokens,
        }

        try:
            data = await post_json(
                f"{self.settings.openrouter_base_url.rstrip('/')}/chat/completions",
                headers=headers,
                payload=payload,
            )
            return data["choices"][0]["message"]["content"]
        except Exception:
            return offline_fallback(route)
