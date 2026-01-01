#!/usr/bin/env sh
# Tiny helper to load local `.env` into the shell and start the Spring Boot app.
# Usage: from repo root run `services/engine/scripts/start-local.sh` or run it directly.

set -e

# Resolve to services/engine directory (script is located in services/engine/scripts)
DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DIR"

if [ ! -f .env ]; then
  echo ".env not found in $DIR. Copy .env.example to .env and edit it before running this script."
  exit 1
fi

echo "Loading .env into environment..."
set -a
. .env
set +a

echo "Starting KindleDrop engine (Gradle bootRun)..."
./gradlew bootRun
