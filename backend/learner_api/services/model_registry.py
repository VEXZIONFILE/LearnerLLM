from dataclasses import dataclass

from learner_api.schemas import AppMode


@dataclass(frozen=True)
class ModelRoute:
    model_id: str
    display_name: str
    temperature: float
    max_tokens: int


TUTOR_MODEL = "openai/gpt-oss-120b"
STUDY_MODEL = "nvidia/nemotron-3-super-120b-a12b"
CODE_MODEL = "poolside/laguna-m.1"

PREMIUM_TIERS = {"BASIC", "PRO"}


def is_premium_tier(tier: str) -> bool:
    return tier in PREMIUM_TIERS


def resolve_model(mode: AppMode, subscription_tier: str) -> ModelRoute:
    premium = is_premium_tier(subscription_tier)
    if mode == AppMode.TUTOR:
        return ModelRoute(
            model_id=TUTOR_MODEL,
            display_name="Learner Tutor",
            temperature=0.7 if premium else 0.65,
            max_tokens=2048 if premium else 1024,
        )
    if mode == AppMode.STUDY:
        return ModelRoute(
            model_id=STUDY_MODEL,
            display_name="Learner Study",
            temperature=0.5 if premium else 0.45,
            max_tokens=3072 if premium else 1536,
        )
    return ModelRoute(
        model_id=CODE_MODEL,
        display_name="Learner Code",
        temperature=0.4 if premium else 0.35,
        max_tokens=2048 if premium else 1024,
    )
