#!/bin/sh
set -e

mkdir -p /app/data /app/secrets

if [ -n "$FIREBASE_CREDENTIALS_JSON" ]; then
  printf '%s' "$FIREBASE_CREDENTIALS_JSON" > /app/secrets/firebase.json
  export FIREBASE_CREDENTIALS_PATH=/app/secrets/firebase.json
fi

if [ -n "$GOOGLE_PLAY_CREDENTIALS_JSON" ]; then
  printf '%s' "$GOOGLE_PLAY_CREDENTIALS_JSON" > /app/secrets/google-play.json
  export GOOGLE_PLAY_CREDENTIALS_PATH=/app/secrets/google-play.json
fi

PORT="${PORT:-8080}"
exec uvicorn learner_api.main:app --host 0.0.0.0 --port "$PORT"
