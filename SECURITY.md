# Security Policy

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a security issue in Learner LM, please open a private security advisory on GitHub or contact the maintainers directly.

## Scope

Learner LM handles student learning data locally on-device. When using OpenRouter:

- `OPENROUTER_API_KEY` should be stored in `local.properties` (never committed to the repository)
- Student conversations are sent to OpenRouter using the `openai/gpt-oss-120b` model
- Review OpenRouter's privacy policy before deploying to students

## Best Practices

- Do not hardcode API keys in source code
- Use `local.properties` for development secrets (gitignored)
- Review AI provider privacy policies before deploying to students
