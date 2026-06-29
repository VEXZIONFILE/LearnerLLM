# Contributing to Learner LM

Thank you for your interest in contributing to Learner LM!

## Development Setup

1. Clone the repository
2. Open in Android Studio Koala or later
3. Sync Gradle and run on an emulator or device

## Code Standards

- Follow Kotlin coding conventions
- Use MVVM architecture: UI → ViewModel → Repository
- All AI interactions must go through `TutorEngine` and `PromptBuilder`
- Never bypass the no-answer educational policy in AI prompts

## Pull Request Guidelines

- Keep changes focused and well-described
- Add unit tests for AI logic changes (`SubjectClassifier`, `PromptBuilder`, `TutorEngine`)
- Ensure `./gradlew test` passes before submitting

## Educational Integrity

Any contribution that weakens the Socratic tutoring safeguards will not be accepted. The AI must never provide direct homework answers.
