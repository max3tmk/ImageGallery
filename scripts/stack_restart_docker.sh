#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

PORTS_SERVICES="8080:authentication-service 8081:image-service 8085:api-gateway 5432:postgres 4566:localstack 3000:frontend"

echo "=== Killing processes on ports (only non-docker processes) ==="
for entry in $PORTS_SERVICES; do
  port="${entry%%:*}"
  service="${entry##*:}"

  pids=$(lsof -ti tcp:"$port" 2>/dev/null || true)
  [ -z "$pids" ] && continue

  output=""
  for pid in $pids; do
    cmd=$(ps -p "$pid" -o comm= 2>/dev/null | tr -d ' ')
    [ -z "$cmd" ] && continue
    if echo "$cmd" | grep -qi "docker"; then
      output+="  Skipping Docker-related: $cmd"$'\n'
    else
      output+="  Killing: $cmd"$'\n'
      kill -9 "$pid" 2>/dev/null || true
    fi
  done

  if [ -n "$output" ]; then
    echo "Found processes on port $port ($service):"
    echo -n "$output"
    echo
  fi
done

echo "=== Stopping and removing containers (keep volumes) ==="
COMPOSE_BAKE=true docker compose down --remove-orphans

echo "=== Building Maven projects ==="
mvn -q clean package -DskipTests

echo "=== Rebuilding and starting containers (including frontend) ==="
COMPOSE_BAKE=true docker compose up --build -d

if COMPOSE_BAKE=true docker compose ps --services | grep -qw localstack; then
  echo "=== Initializing LocalStack ==="
  until COMPOSE_BAKE=true docker exec localstack awslocal s3 ls >/dev/null 2>&1; do
    echo "Waiting for LocalStack S3 to be ready..."
    sleep 2
  done
  echo "LocalStack S3 is ready."

  echo "=== Creating bucket 'images' (idempotent) ==="
  COMPOSE_BAKE=true docker exec localstack awslocal s3 mb s3://images 2>/dev/null || true

  echo "=== Uploading test file ==="
  echo "test" > /tmp/test.txt
  COMPOSE_BAKE=true docker cp /tmp/test.txt localstack:/tmp/test.txt
  COMPOSE_BAKE=true docker exec localstack awslocal s3 cp /tmp/test.txt s3://images/test.txt >/dev/null
fi

echo "=== Stack restarted successfully (db persisted) ==="
