from dataclasses import dataclass
from datetime import datetime


@dataclass
class User:
    uid: str
    email: str = ""
    display_name: str = "Student"
    photo_url: str | None = None
    grade_level: int = 8
    subscription_tier: str = "FREE"
    created_at: datetime | None = None
    updated_at: datetime | None = None


@dataclass
class CustomSubject:
    id: int
    user_uid: str
    name: str
    category: str
    emoji: str
    created_at: datetime | None = None


@dataclass
class ChatSession:
    id: str
    user_uid: str
    title: str
    created_at: datetime | None = None
    updated_at: datetime | None = None


@dataclass
class ChatMessage:
    id: int
    session_id: str
    role: str
    content: str
    subject_key: str
    hint_level: int
    created_at: datetime | None = None


@dataclass
class StudyTopic:
    id: int
    user_uid: str
    name: str
    subject_key: str
    strength_score: float
    last_studied_at: datetime | None = None


@dataclass
class LearningStreak:
    user_uid: str
    current_streak: int = 0
    longest_streak: int = 0
    last_active_date: str = ""


@dataclass
class ScanUsage:
    id: int
    user_uid: str
    usage_date: str
    count: int
    updated_at: datetime | None = None


@dataclass
class SubscriptionRecord:
    id: int
    user_uid: str
    product_id: str
    purchase_token: str
    tier: str
    verified: bool
    expires_at: datetime | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None
