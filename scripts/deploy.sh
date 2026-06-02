#!/usr/bin/env bash
# Deploy helper — run on the target host from the repo root.
# Usage:
#   ./scripts/deploy.sh                 # production compose
#   ./scripts/deploy.sh dev             # base + dev overrides
set -euo pipefail

MODE="${1:-prod}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example and set secrets first." >&2
  exit 1
fi

case "$MODE" in
  prod)
    echo "==> Production deploy"
    docker compose -f docker-compose.prod.yml pull || true
    docker compose -f docker-compose.prod.yml up --build -d
    docker compose -f docker-compose.prod.yml ps
    ;;
  dev)
    echo "==> Development stack"
    docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
    docker compose ps
    ;;
  *)
    echo "Usage: $0 [prod|dev]" >&2
    exit 1
    ;;
esac

echo "Done. Frontend: http://localhost:${FRONTEND_PORT:-80}"
