# KindleDrop

Drop a GitHub link. Read it on your Kindle.

KindleDrop is a **local-first** tool:

- A TypeScript CLI (`stk`) for a nice terminal UX
- A Spring Boot (Gradle) local engine for **secure downloading** + **SMTP sending**

> ⚠️ Use this only for content you have the right to download and email to your Kindle.

---

## Features (MVP)

- Accepts GitHub file links:
  - `https://github.com/<org>/<repo>/blob/<branch>/path/file.pdf`
  - `https://raw.githubusercontent.com/<org>/<repo>/<branch>/path/file.pdf`
- Resolves to a direct download URL
- Downloads with safety limits:
  - GitHub host allowlist
  - max size (default 30 MB)
  - timeouts
- Emails as attachment to your Kindle address via SMTP
- Deletes temp files after send

---

## Prerequisites

- Node.js 18+ (or 20+)
- Java 21+ (Java 17 also works, but Java 21 recommended)
- An SMTP account (e.g. Gmail App Password, SES, etc.)
- Your Kindle “Send to Kindle” email address, and your sender email approved in Amazon settings:
  **Manage Your Content and Devices → Preferences → Personal Document Settings → Approved Personal Document E-mail List**

---

## Quick start

### 1) Start the engine (Spring Boot)

```bash
cd services/engine
./gradlew bootRun
```

Engine runs at: `http://localhost:8080`

### 2) Configure CLI

Create config:

```bash
mkdir -p ~/.kindledrop
cat > ~/.kindledrop/config.json <<'JSON'
{
  "engineUrl": "http://localhost:8080",
  "kindleEmail": "yourname_123@kindle.com",
  "smtp": {
    "host": "smtp.gmail.com",
    "port": 587,
    "user": "you@gmail.com",
    "from": "you@gmail.com",
    "useStartTLS": true
  },
  "limits": {
    "maxBytes": 31457280
  }
}
JSON
```

Set SMTP password (recommended as env var):

```bash
export KINDLEDROP_SMTP_PASS="your-smtp-password-or-app-password"
```

### 3) Run the CLI

```bash
cd apps/cli
npm i
npm run dev -- send "https://github.com/org/repo/blob/main/path/book.pdf"
```

Or build & install globally (optional):

```bash
npm run build
npm link
stk send "https://github.com/org/repo/blob/main/path/book.pdf"
```

---

## CLI usage

```bash
stk send <github-url> [options]

Options:
  --to <email>        Override Kindle email (otherwise uses config)
  --engine <url>      Override engine URL (otherwise uses config)
  --dry-run           Resolve + validate only (no download, no email)
  -v, --verbose       Show extra debug output
```

---

## Engine API

`POST /api/send`

Request:

```json
{
  "url": "https://github.com/org/repo/blob/main/path/book.pdf",
  "kindleEmail": "yourname_123@kindle.com",
  "dryRun": false
}
```

Response:

```json
{
  "ok": true,
  "resolvedUrl": "https://raw.githubusercontent.com/org/repo/main/path/book.pdf",
  "fileName": "book.pdf",
  "bytes": 123456
}
```

---

## Notes

- Default allowed extensions: `pdf`, `epub`, `mobi`, `azw3`
- Default allowlisted hosts: `github.com`, `raw.githubusercontent.com`
- To support GitHub release assets later, we can add `objects.githubusercontent.com` and redirect-safe handling.

---

## Development tips

- If you use Gmail, prefer an **App Password** (not your normal password).
- If SMTP auth fails, verify:
  - `KINDLEDROP_SMTP_PASS` is set
  - your provider supports the chosen port + STARTTLS/SSL

### Dev tooling (dev-mcp)

After cloning:

```
git submodule update --init --recursive
cd tools/dev-mcp && npm i && npm run build
```

---

## License

Personal tool template — add your preferred license.
