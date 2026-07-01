from pydantic import BaseModel, Field


class Settings(BaseModel):
    app_name: str = "LearnerLM API"
    cors_origins: str = "*"

    openrouter_api_key: str = ""
    openrouter_base_url: str = "https://openrouter.ai/api/v1"
    openrouter_referer: str = "https://github.com/VEXZIONFILE/LearnerLLM"
    openrouter_title: str = "LearnerLM"

    firebase_project_id: str = ""
    firebase_auth_disabled: bool = False
    dev_auth_uid: str = "dev-user"

    google_play_package_name: str = "com.learnerlm"
    billing_verification_disabled: bool = True
    free_daily_scan_limit: int = 3

    @classmethod
    def from_env(cls, env) -> "Settings":
        def _bool(name: str, default: bool = False) -> bool:
            raw = getattr(env, name, None)
            if raw is None:
                return default
            return str(raw).lower() in {"1", "true", "yes", "on"}

        def _int(name: str, default: int) -> int:
            raw = getattr(env, name, None)
            if raw is None:
                return default
            try:
                return int(raw)
            except (TypeError, ValueError):
                return default

        def _str(name: str, default: str = "") -> str:
            raw = getattr(env, name, None)
            return str(raw) if raw is not None else default

        return cls(
            app_name=_str("APP_NAME", "LearnerLM API"),
            cors_origins=_str("CORS_ORIGINS", "*"),
            openrouter_api_key=_str("OPENROUTER_API_KEY"),
            openrouter_base_url=_str("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1"),
            openrouter_referer=_str("OPENROUTER_REFERER", "https://github.com/VEXZIONFILE/LearnerLLM"),
            openrouter_title=_str("OPENROUTER_TITLE", "LearnerLM"),
            firebase_project_id=_str("FIREBASE_PROJECT_ID"),
            firebase_auth_disabled=_bool("FIREBASE_AUTH_DISABLED"),
            dev_auth_uid=_str("DEV_AUTH_UID", "dev-user"),
            google_play_package_name=_str("GOOGLE_PLAY_PACKAGE_NAME", "com.learnerlm"),
            billing_verification_disabled=_bool("BILLING_VERIFICATION_DISABLED", True),
            free_daily_scan_limit=_int("FREE_DAILY_SCAN_LIMIT", 3),
        )


def get_settings_from_request(request) -> Settings:
    env = request.scope["env"]
    return Settings.from_env(env)
