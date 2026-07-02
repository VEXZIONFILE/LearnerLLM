from dataclasses import dataclass

from learner_api.schemas import AppMode, FreeModelVariant


@dataclass(frozen=True)
class ModelRoute:
    model_id: str
    display_name: str
    temperature: float
    max_tokens: int


TUTOR_MODEL = "openai/gpt-oss-120b"
STUDY_MODEL = "nvidia/nemotron-3-super-120b-a12b"
CODE_MODEL = "poolside/laguna-m.1"

FREE_TUTOR_MODEL = "openai/gpt-oss-120b:free"
FREE_STUDY_MODEL = "nvidia/nemotron-3-super-120b-a12b:free"
FREE_CODE_MODEL = "poolside/laguna-m.1:free"

PREMIUM_TIERS = {"BASIC", "PRO"}


def is_premium_tier(tier: str) -> bool:
    return tier in PREMIUM_TIERS


def is_pro_tier(tier: str) -> bool:
    return tier == "PRO"


def default_variant_for_mode(mode: AppMode) -> FreeModelVariant:
    if mode == AppMode.STUDY:
        return FreeModelVariant.STUDY
    if mode == AppMode.CODE:
        return FreeModelVariant.CODE
    return FreeModelVariant.TUTOR


def resolve_free_variant(variant: FreeModelVariant) -> ModelRoute:
    if variant == FreeModelVariant.STUDY:
        return ModelRoute(
            model_id=FREE_STUDY_MODEL,
            display_name="Nemotron",
            temperature=0.45,
            max_tokens=1536,
        )
    if variant == FreeModelVariant.CODE:
        return ModelRoute(
            model_id=FREE_CODE_MODEL,
            display_name="Laguna",
            temperature=0.35,
            max_tokens=1024,
        )
    return ModelRoute(
        model_id=FREE_TUTOR_MODEL,
        display_name="GPT-OSS",
        temperature=0.65,
        max_tokens=1024,
    )


def resolve_model(
    mode: AppMode,
    subscription_tier: str,
    free_model_variant: FreeModelVariant | None = None,
) -> ModelRoute:
    if subscription_tier == "FREE":
        variant = free_model_variant or default_variant_for_mode(mode)
        return resolve_free_variant(variant)

    pro = is_pro_tier(subscription_tier)
    premium = is_premium_tier(subscription_tier)
    if mode == AppMode.TUTOR or mode == AppMode.FREE:
        return ModelRoute(
            model_id=TUTOR_MODEL,
            display_name="Learner Tutor",
            temperature=0.75 if pro else (0.7 if premium else 0.65),
            max_tokens=3072 if pro else (2048 if premium else 1024),
        )
    if mode == AppMode.STUDY:
        return ModelRoute(
            model_id=STUDY_MODEL,
            display_name="Learner Study",
            temperature=0.55 if pro else (0.5 if premium else 0.45),
            max_tokens=4096 if pro else (3072 if premium else 1536),
        )
    return ModelRoute(
        model_id=CODE_MODEL,
        display_name="Learner Code",
        temperature=0.45 if pro else (0.4 if premium else 0.35),
        max_tokens=3072 if pro else (2048 if premium else 1024),
    )
