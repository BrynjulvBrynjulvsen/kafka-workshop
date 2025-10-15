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

ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin-secret}"
CLIENT_USER="${CLIENT_USER:-workshop-client}"
CLIENT_PASSWORD="${CLIENT_PASSWORD:-workshop-secret}"
TOPIC_NAME="${TOPIC_NAME:-scram-demo}"
GROUP_ID="${GROUP_ID:-scram-demo-group}"

COMPOSE_FILES=(
  -f "${ROOT_DIR}/docker-compose.yml"
  -f "${ROOT_DIR}/docker-compose.scram.yml"
)

echo "Ensuring SCRAM credentials for admin user (${ADMIN_USER})..."
"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 \
  kafka-configs --bootstrap-server kafka1:9092 \
  --alter --add-config "SCRAM-SHA-512=[password=${ADMIN_PASSWORD}]" \
  --entity-type users --entity-name "${ADMIN_USER}"

echo "Ensuring SCRAM credentials for workshop client (${CLIENT_USER})..."
"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 \
  kafka-configs --bootstrap-server kafka1:9092 \
  --alter --add-config "SCRAM-SHA-512=[password=${CLIENT_PASSWORD}]" \
  --entity-type users --entity-name "${CLIENT_USER}"

ADMIN_PROPS_PATH="/tmp/${ADMIN_USER}-admin.properties"

echo "Writing temporary admin properties to ${ADMIN_PROPS_PATH} (inside kafka1 container)..."
"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 bash -c "cat <<'EOF' > ${ADMIN_PROPS_PATH}
sasl.mechanism=SCRAM-SHA-512
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username='${ADMIN_USER}' password='${ADMIN_PASSWORD}';
EOF"

CLIENT_PROPS_PATH="/tmp/${CLIENT_USER}-sasl.properties"

echo "Writing workshop client properties to ${CLIENT_PROPS_PATH}..."
"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 bash -c "cat <<'EOF' > ${CLIENT_PROPS_PATH}
sasl.mechanism=SCRAM-SHA-512
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username='${CLIENT_USER}' password='${CLIENT_PASSWORD}';
EOF"

echo "Creating ACLs for ${CLIENT_USER} on topic ${TOPIC_NAME} and group ${GROUP_ID}..."
"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 \
  kafka-acls --bootstrap-server kafka1:9096 \
  --command-config "${ADMIN_PROPS_PATH}" \
  --add --allow-principal "User:${CLIENT_USER}" \
  --operation Read --operation Write --operation Describe \
  --topic "${TOPIC_NAME}"

"${COMPOSE[@]}" "${COMPOSE_FILES[@]}" exec kafka1 \
  kafka-acls --bootstrap-server kafka1:9096 \
  --command-config "${ADMIN_PROPS_PATH}" \
  --add --allow-principal "User:${CLIENT_USER}" \
  --group "${GROUP_ID}" \
  --operation Read

echo "SCRAM users and ACLs created."
echo "Admin properties: ${ADMIN_PROPS_PATH}"
echo "Client properties: ${CLIENT_PROPS_PATH}"
