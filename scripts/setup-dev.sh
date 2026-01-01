#!/usr/bin/env bash
set -euo pipefail

git submodule update --init --recursive
cd tools/dev-mcp
npm i
npm run build
