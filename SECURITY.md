# Security Policy

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a security issue in Learner LM, please open a private security advisory on GitHub or contact the maintainers directly.

## Scope

Learner LM handles student learning data locally on-device. When using cloud AI APIs:

- API keys should be stored securely (never committed to the repository)
- Student conversations may be sent to third-party AI providers — review your provider's data policies
- Firebase Auth/Firestore (optional) should use proper security rules

## Best Practices

- Do not hardcode API keys in source code
- Use `local.properties` for development secrets (gitignored)
- Review AI provider privacy policies before deploying to students
