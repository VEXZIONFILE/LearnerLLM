CREATE TABLE IF NOT EXISTS content_reports (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uid TEXT NOT NULL REFERENCES users(uid),
  session_id TEXT,
  message_id INTEGER,
  content TEXT NOT NULL,
  reason TEXT NOT NULL,
  details TEXT,
  app_mode TEXT,
  status TEXT NOT NULL DEFAULT 'open',
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_content_reports_user ON content_reports(user_uid);
CREATE INDEX IF NOT EXISTS idx_content_reports_status ON content_reports(status);
