from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "LearnerLM API"
    debug: bool = False
    database_url: str = "sqlite+aiosqlite:///./data/learnerlm.db"
    cors_origins: str = "*"

    openrouter_api_key: str = ""
    openrouter_base_url: str = "https://openrouter.ai/api/v1"
    openrouter_referer: str = "https://github.com/VEXZIONFILE/LearnerLLM"
    openrouter_title: str = "LearnerLM"

    firebase_project_id: str = ""
    firebase_credentials_path: str = ""
    firebase_credentials_json: str = ""
    firebase_auth_disabled: bool = False
    dev_auth_uid: str = "dev-user"

    google_play_package_name: str = "com.learnerlm"
    google_play_credentials_path: str = ""
    billing_verification_disabled: bool = True

    free_daily_scan_limit: int = 3


@lru_cache
def get_settings() -> Settings:
    return Settings()
