from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field


class AppMode(str, Enum):
    TUTOR = "TUTOR"
    STUDY = "STUDY"
    CODE = "CODE"


class SubjectCategory(str, Enum):
    CLASS = "CLASS"
    AFTER_SCHOOL = "AFTER_SCHOOL"
    PROJECT = "PROJECT"
    CLUB = "CLUB"
    EXAM_PREP = "EXAM_PREP"
    OTHER = "OTHER"


class BuiltinSubject(str, Enum):
    MATH = "MATH"
    SCIENCE = "SCIENCE"
    ENGLISH = "ENGLISH"
    HISTORY = "HISTORY"
    GEOGRAPHY = "GEOGRAPHY"
    GENERAL = "GENERAL"


class HintLevel(int, Enum):
    GENTLE_NUDGE = 1
    DEEPER_EXPLANATION = 2
    NEAR_SOLUTION = 3
    STUDENT_ATTEMPT_REQUIRED = 4


class UserProfileResponse(BaseModel):
    uid: str
    email: str
    display_name: str
    photo_url: str | None = None
    grade_level: int
    subscription_tier: str
    created_at: datetime


class UpdateProfileRequest(BaseModel):
    grade_level: int | None = Field(default=None, ge=6, le=12)
    display_name: str | None = Field(default=None, min_length=1, max_length=120)


class CustomSubjectResponse(BaseModel):
    id: int
    name: str
    category: SubjectCategory
    emoji: str
    storage_key: str


class CreateCustomSubjectRequest(BaseModel):
    name: str = Field(min_length=1, max_length=40)
    category: SubjectCategory
    emoji: str | None = None


class StudySubjectInput(BaseModel):
    kind: str = Field(description="builtin or custom")
    builtin: BuiltinSubject | None = None
    custom_id: int | None = None
    custom_name: str | None = None
    custom_category: SubjectCategory | None = None


class ChatMessageInput(BaseModel):
    role: str
    content: str


class ChatRequest(BaseModel):
    session_id: str | None = None
    grade_level: int = Field(ge=6, le=12, default=8)
    app_mode: AppMode = AppMode.TUTOR
    hint_level: HintLevel = HintLevel.GENTLE_NUDGE
    subject: StudySubjectInput | None = None
    student_message: str = Field(min_length=1)
    conversation_history: list[ChatMessageInput] = Field(default_factory=list)
    scanned_text: str | None = None


class ChatResponse(BaseModel):
    session_id: str
    message: str
    hint_level: HintLevel
    subject_key: str
    subject_display_name: str
    model_label: str
    detected_mistake: str | None = None
    encourages_attempt: bool = True


class ChatSessionResponse(BaseModel):
    id: str
    title: str
    created_at: datetime
    updated_at: datetime


class ChatMessageResponse(BaseModel):
    id: int
    role: str
    content: str
    subject_key: str
    hint_level: int
    created_at: datetime


class ScanQuotaResponse(BaseModel):
    used_today: int
    daily_limit: int | None
    remaining: int | None
    is_premium: bool
    can_scan: bool
    quota_label: str


class RecordScanRequest(BaseModel):
    extracted_text_length: int | None = Field(default=None, ge=0)


class BillingVerifyRequest(BaseModel):
    product_id: str
    purchase_token: str
    package_name: str | None = None


class BillingVerifyResponse(BaseModel):
    verified: bool
    subscription_tier: str
    product_id: str


class StudyTopicResponse(BaseModel):
    id: int
    name: str
    subject_key: str
    strength_score: float
    last_studied_at: datetime


class LearningStreakResponse(BaseModel):
    current_streak: int
    longest_streak: int
    last_active_date: str


class ProgressResponse(BaseModel):
    topics: list[StudyTopicResponse]
    streak: LearningStreakResponse
    weak_topics: list[StudyTopicResponse]


class HealthResponse(BaseModel):
    status: str
    app: str


class ReportReason(str, Enum):
    OFFENSIVE = "OFFENSIVE"
    HARMFUL = "HARMFUL"
    INACCURATE = "INACCURATE"
    SPAM = "SPAM"
    OTHER = "OTHER"


class ReportContentRequest(BaseModel):
    session_id: str | None = None
    message_id: int | None = None
    content: str = Field(min_length=1, max_length=8000)
    reason: ReportReason
    details: str | None = Field(default=None, max_length=1000)
    app_mode: str | None = None


class ReportContentResponse(BaseModel):
    report_id: int
    status: str = "received"
