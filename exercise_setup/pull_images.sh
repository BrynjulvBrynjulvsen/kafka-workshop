#!/usr/bin/env bash

set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but was not found on PATH." >&2
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  echo "docker compose (or docker-compose) is required but was not found." >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Pulling workshop images defined in docker-compose.yml..."
"${COMPOSE[@]}" -f "${ROOT_DIR}/docker-compose.yml" pull

echo "Building custom Flink image..."
COMPOSE_DOCKER_CLI_BUILD=1 BUILDKIT_PROGRESS=plain \
  "${COMPOSE[@]}" \
  -f "${ROOT_DIR}/docker-compose.yml" \
  -f "${ROOT_DIR}/docker-compose.flink.yml" \
  build flink-jobmanager

echo "Docker images prepared for the workshop."
