# KindleDrop — Project Context

## Goal

Local-first tool to download GitHub file links (PDF/e-books) and send to Kindle via SMTP.

## Architecture

- TypeScript CLI (`apps/cli`)
- Spring Boot (Gradle) engine (`services/engine`)

## Standards (do not re-discuss)

- `.gitignore` + `.editorconfig` exist
- Prettier + ESLint aligned with `.editorconfig`
- Prefer full-file replacements when updating code

## Rules

- Security-aware: allowlists, size/time limits, no secrets in repo
- Clear error handling: return friendly JSON errors (avoid raw stack traces)
- Keep CLI UX simple and predictable
