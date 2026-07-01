# Learner LM 🎓

An AI-powered learning companion for students in grades 6–12.

> **Download for Android Studio:** [Download ZIP](https://github.com/VEXZIONFILE/LearnerLLM/archive/refs/heads/cursor/learner-lm-android-scaffold-6bf2.zip) · See [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md) for step-by-step instructions.

Learner LM is an intelligent tutoring assistant inspired by NotebookLM, designed to guide students through problem-solving and concept learning using a Socratic method. It helps students learn how to think through problems, not just get answers.

## Core Philosophy

Learner LM is **NOT** a homework solver.

It is: *"A thinking partner that helps students learn how to solve problems, not just get answers."*

### Key Principles

- ❌ Never provides final answers
- ✅ Always uses hints, questions, and step-by-step guidance
- 📚 Adapts to grade level (6–12)
- 🧠 Uses Socratic method for active learning
- 🔐 Enforces educational integrity

## Features

### Core Features

- **AI Study Chat** — ChatGPT-style interface with image + text support
- **Homework Scanner** — Capture and analyze worksheet/textbook pages (ML Kit OCR)
- **Step-by-Step Learning** — Break problems into manageable components
- **Concept Explainer** — Progressive explanation (simple → intermediate → advanced)
- **Mistake Detection** — Identify and guide correction of reasoning errors
- **Practice Generator** — Create similar problems for reinforcement
- **Progress Tracker** — Monitor learning topics and identify weaknesses
- **Custom Subjects** — Kids and teens can add their own subjects (class, after-school, projects, clubs, etc.)

### Advanced Features

- 🎙️ Voice Tutor Mode — Voice input and output (planned)
- 🔥 Streak System — Daily learning motivation
- 🧠 Weakness Detection — AI-powered learning gap identification
- 📴 Offline Mode — Cached explanations and study guides

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Android** | Kotlin, Jetpack Compose, MVVM, Room |
| **Backend (production)** | Render — FastAPI (`backend/`) |
| **Backend (local dev)** | FastAPI, SQLite (`backend/`) |
| Image Processing | ML Kit OCR (on-device) |
| Networking | Retrofit → LearnerLM API |
| AI Models | gpt-oss-120b, Nemotron 3 Super, Laguna M.1 (via OpenRouter on server) |
| Authentication | Firebase Auth + server token verification |
| Billing | Google Play + server purchase verification |

## Project Structure

```
LearnerLM/
├── app/                    # Android app (Kotlin / Compose)
├── backend/                # FastAPI API (local dev + Render production)
│   ├── learner_api/
│   └── tests/
├── render.yaml             # Render blueprint
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Getting Started

### Prerequisites

- Android Studio Koala or later
- Kotlin 2.2+
- Gradle 8.0+
- Java 11+

### Clone & Setup

```bash
git clone https://github.com/VEXZIONFILE/LearnerLM.git
cd LearnerLM
```

### Build & Run

```bash
# Build debug APK
./gradlew build

# Run on emulator/device
./gradlew installDebug

# Run tests
./gradlew test
```

### Backend + Android setup

1. **Production:** deploy [`backend/`](backend/RENDER_DEPLOY.md) to [Render](https://render.com) (see `render.yaml`).

2. **Local dev** (see [backend/README.md](backend/README.md)):

```bash
cd backend
cp .env.example .env
pip install -r requirements.txt
uvicorn learner_api.main:app --host 0.0.0.0 --port 8080
```

3. **Configure the Android app** in `local.properties`:

```properties
LEARNER_API_BASE_URL=http://10.0.2.2:8080/
```

| Setting | Value |
|---------|-------|
| API Base URL | Your LearnerLM backend (e.g. `http://10.0.2.2:8080/`) |
| OpenRouter key | Backend `.env` only — never in the APK |
| Auth | Firebase ID token sent on every request |

See [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md) for the full walkthrough.

### Custom Subjects

Students can tap **"Add yours"** on the chat screen to create personal subjects like:

- 🏫 Class (e.g. "AP Biology")
- 🎨 After School (e.g. "Drama Club")
- 📋 Project (e.g. "Science Fair")
- ⚽ Club / Activity
- 📝 Exam Prep
- ✨ Other

Custom subjects are saved locally and tailor the AI tutor's guidance to that context.

### Email Sign-In & Subscriptions

- **Email & password** via Firebase Auth — stays signed in after closing the app
- **Subscriptions** via Google Play Billing:
  - **Pro** — **$9.99/month** (`learnerlm_basic_monthly`)
  - **Premium** — **$14.99/month** (`learnerlm_pro_monthly`)
  - **Mega** — **$190.00/year** (`learnerlm_pro_yearly` — pay for 10 months, get 12; 2 months free)

## AI Tutor Engine

The core AI behavior follows the **Socratic Method** with a **Hint Ladder System**:

| Level | Description |
|-------|-------------|
| Hint 1 | Gentle directional nudge |
| Hint 2 | Deeper explanation |
| Hint 3 | Near-solution guidance (incomplete) |
| Final | Student attempt required |

### Subject-Specific Tutoring Logic

| Subject | Approach |
|---------|----------|
| Math | Focus on reasoning, formula discovery, no computation answers |
| Science | Process explanation, cause-effect, analogies |
| English | Grammar hints, writing structure, no essay solutions |
| History | Timeline reasoning, context explanation |
| Geography | Spatial reasoning, location analysis |

## Safety Guarantees

The AI system enforces:

- No direct answers to homework
- Grade-level adaptation (6–12)
- Subject classification for appropriate tutoring
- Anti-gaming safeguards against answer-seeking

## Critical Educational Rules (NON-NEGOTIABLE)

The AI **MUST NEVER**:

- ❌ Solve math problems directly
- ❌ Provide final essay answers
- ❌ Output completed homework solutions
- ❌ Say "the answer is…"

The AI **MUST**:

- ✅ Teach step-by-step thinking
- ✅ Provide hints only
- ✅ Ask guiding questions
- ✅ Break problems into smaller parts
- ✅ Help students discover answers themselves

## Example Conversation

**Student:** "What is 2 + 2?"

**Learner LM:**

> Let me understand your question first. You're asking me to add 2 and 2.
>
> Here's a hint to get you started: Imagine you have 2 apples in one hand and 2 apples in your other hand. How many apples do you have in total?
>
> Try counting them out, and then tell me: What number did you get?

**Student:** "I think it's 4?"

**Learner LM:**

> Excellent! You discovered that 2 + 2 = 4. 🎉
>
> You used counting to solve the problem. This is the foundation of addition. Now, think about this: What if you had 3 apples in one hand and 4 in the other—how would you find the total?
>
> Try using the same method!

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

Built with ❤️ to empower student learning
