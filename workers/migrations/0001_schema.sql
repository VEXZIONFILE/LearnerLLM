CREATE TABLE IF NOT EXISTS users (
  uid TEXT PRIMARY KEY,
  email TEXT NOT NULL DEFAULT '',
  display_name TEXT NOT NULL DEFAULT 'Student',
  photo_url TEXT,
  grade_level INTEGER NOT NULL DEFAULT 8,
  subscription_tier TEXT NOT NULL DEFAULT 'FREE',
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS custom_subjects (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  emoji TEXT NOT NULL DEFAULT '✨',
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS chat_sessions (
  id TEXT PRIMARY KEY,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  title TEXT NOT NULL DEFAULT 'Chat',
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS chat_messages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  session_id TEXT NOT NULL REFERENCES chat_sessions(id),
  role TEXT NOT NULL,
  content TEXT NOT NULL,
  subject_key TEXT NOT NULL DEFAULT 'builtin:GENERAL',
  hint_level INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS study_topics (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  name TEXT NOT NULL,
  subject_key TEXT NOT NULL,
  strength_score REAL NOT NULL DEFAULT 0.4,
  last_studied_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE(user_uid, subject_key)
);

CREATE TABLE IF NOT EXISTS learning_streaks (
  user_uid TEXT PRIMARY KEY REFERENCES users(uid),
  current_streak INTEGER NOT NULL DEFAULT 0,
  longest_streak INTEGER NOT NULL DEFAULT 0,
  last_active_date TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS scan_usage (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  usage_date TEXT NOT NULL,
  count INTEGER NOT NULL DEFAULT 0,
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE(user_uid, usage_date)
);

CREATE TABLE IF NOT EXISTS subscription_records (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  product_id TEXT NOT NULL,
  purchase_token TEXT NOT NULL,
  tier TEXT NOT NULL,
  verified INTEGER NOT NULL DEFAULT 0,
  expires_at TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_custom_subjects_user ON custom_subjects(user_uid);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user ON chat_sessions(user_uid);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_study_topics_user ON study_topics(user_uid);
CREATE INDEX IF NOT EXISTS idx_scan_usage_user_date ON scan_usage(user_uid, usage_date);
CREATE INDEX IF NOT EXISTS idx_subscription_user ON subscription_records(user_uid);
