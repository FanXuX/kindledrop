# KindleDrop Engine — Local SMTP configuration

This document explains the recommended way to provide SMTP credentials for local development.

Important: do NOT commit secrets. Use a local `.env` file (gitignored) and load it into your shell before starting the app.

## Create a local `.env`

Copy the example and fill in your SMTP values:

cd services/engine
cp .env.example .env
# edit .env and fill values
```

## Load `.env` into your shell (zsh / bash)

This exports the variables into your current shell so the Gradle/Java process inherits them.

```bash
cd services/engine
set -a           # export all variables
source .env
set +a           # stop exporting

# then start the app (env vars are visible to the process)
./gradlew bootRun
```

Alternatively, start with `./gradlew test` or other commands — any process started from the same shell will inherit the variables.

## Notes

- The engine reads SMTP config from environment variables:

  - `KINDLEDROP_SMTP_HOST`
  - `KINDLEDROP_SMTP_PORT` (optional, defaults to `587`)
  - `KINDLEDROP_SMTP_USER`
  - `KINDLEDROP_SMTP_FROM`
  - `KINDLEDROP_SMTP_PASS`
  - `KINDLEDROP_SMTP_STARTTLS` (optional, defaults to `true`)
  - `KINDLEDROP_SMTP_SSL` (optional, defaults to `false`)

- Do NOT put SMTP credentials in `application.yml` or frontend code.
- For production, use a secrets manager or set real environment variables in your deployment environment.
