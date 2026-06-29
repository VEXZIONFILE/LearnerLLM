# Learner LM 🎓

An AI-powered learning companion for students in grades 6–12.

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

### Advanced Features

- 🎙️ Voice Tutor Mode — Voice input and output (planned)
- 🔥 Streak System — Daily learning motivation
- 🧠 Weakness Detection — AI-powered learning gap identification
- 📴 Offline Mode — Cached explanations and study guides

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| Database | Room (SQLite) |
| Image Processing | ML Kit OCR |
| Networking | Retrofit + OpenRouter API |
| AI Model | `openai/gpt-oss-120b` (displayed as **LearnerLM**) |
| Authentication | Firebase Auth (optional) |
| Sync | Firebase Firestore (optional) |

## Project Structure

```
LearnerLM/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/learner/lm/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── viewmodel/
│   │   │   │   ├── repository/
│   │   │   │   ├── database/
│   │   │   │   ├── ai/
│   │   │   │   │   ├── TutorEngine.kt
│   │   │   │   │   ├── PromptBuilder.kt
│   │   │   │   │   └── SubjectClassifier.kt
│   │   │   │   ├── ocr/
│   │   │   │   ├── utils/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Getting Started

### Prerequisites

- Android Studio Koala or later
- Kotlin 1.9+
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

### AI API Configuration (OpenRouter)

Learner LM uses [OpenRouter](https://openrouter.ai) with the **OpenAI gpt-oss-120b** model, branded in-app as **LearnerLM**.

Set your OpenRouter API key in `local.properties`:

```properties
OPENROUTER_API_KEY=sk-or-v1-your-key-here
```

| Setting | Value |
|---------|-------|
| API Base URL | `https://openrouter.ai/api/v1/` |
| Model ID | `openai/gpt-oss-120b` |
| Display Name | LearnerLM |

Get an API key at https://openrouter.ai/settings/keys

The app includes an offline fallback tutor mode when no API key is configured.

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
