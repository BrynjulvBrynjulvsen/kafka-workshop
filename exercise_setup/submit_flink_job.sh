#!/usr/bin/env bash

set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but was not found on PATH." >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

JAR_PATH="${ROOT_DIR}/build/libs/kafka-workshop-flink-exercises.jar"

if [[ ! -f "${JAR_PATH}" ]]; then
  echo "Jar not found at ${JAR_PATH}. Run './gradlew flinkShadow' first." >&2
  exit 1
fi

ARTIFACT_NAME="$(basename "${JAR_PATH}")"
CONTAINER_PATH="/tmp/${ARTIFACT_NAME}"

docker compose -f "${ROOT_DIR}/docker-compose.yml" -f "${ROOT_DIR}/docker-compose.flink.yml" cp \
  "${JAR_PATH}" "flink-jobmanager:${CONTAINER_PATH}"

FLINK_ARGS=()
JOB_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --)
      shift
      if [[ $# -gt 0 ]]; then
        JOB_ARGS=("${@}")
      fi
      break
      ;;
    *)
      FLINK_ARGS+=("$1")
      ;;
  esac
  shift
done

KAFKA_BOOTSTRAP_SERVERS_DEFAULT="kafka1:9092"

if [[ -z "${KAFKA_BOOTSTRAP_SERVERS:-}" ]]; then
  KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS_DEFAULT}"
fi

exec_cmd=(docker compose -f "${ROOT_DIR}/docker-compose.yml" -f "${ROOT_DIR}/docker-compose.flink.yml" exec \
  -e "KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}")

exec_cmd+=(flink-jobmanager ./bin/flink run)

if (( ${#FLINK_ARGS[@]} > 0 )); then
  exec_cmd+=("${FLINK_ARGS[@]}")
fi

exec_cmd+=("${CONTAINER_PATH}")

if (( ${#JOB_ARGS[@]} > 0 )); then
  exec_cmd+=("${JOB_ARGS[@]}")
fi

"${exec_cmd[@]}"
