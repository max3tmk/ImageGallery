#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

PORTS_SERVICES="8080:authentication-service 8081:image-service 8082:activity-service 8085:api-gateway 5432:postgres 4566:localstack 3000:frontend 9092:kafka 27017:mongo 2181:zookeeper"

echo "=== Checking and killing processes on ports ==="
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

echo "=== Stopping existing infrastructure containers ==="
docker compose stop postgres mongo localstack kafka zookeeper

echo "=== Starting infrastructure services ==="
docker compose up -d postgres mongo localstack kafka zookeeper

echo "=== Waiting for infrastructure to be ready ==="
echo "Waiting for PostgreSQL..."
until docker compose exec postgres pg_isready -U postgres >/dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done
echo "PostgreSQL is ready"

echo "Waiting for LocalStack..."
until docker compose exec localstack awslocal s3 ls >/dev/null 2>&1; do
  echo "Waiting for LocalStack..."
  sleep 2
done

if ! docker compose exec localstack awslocal s3api head-bucket --bucket images >/dev/null 2>&1; then
  echo "Creating S3 bucket 'images'"
  docker compose exec localstack awslocal s3 mb s3://images
fi

echo "Uploading test file..."
timestamp=$(date +%s)
echo "test" > /tmp/test_${timestamp}.txt
docker cp /tmp/test_${timestamp}.txt localstack:/tmp/test_${timestamp}.txt
docker compose exec localstack awslocal s3 cp /tmp/test_${timestamp}.txt s3://images/test_${timestamp}.txt >/dev/null
rm /tmp/test_${timestamp}.txt
echo "Test file uploaded: test_${timestamp}.txt"

echo "LocalStack is ready"

echo "Waiting for Zookeeper..."
until nc -z localhost 2181; do
  echo "Waiting for Zookeeper port 2181..."
  sleep 2
done
echo "Zookeeper port is ready"

echo "Waiting for Kafka..."
until docker compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9093 >/dev/null 2>&1; do
  echo "Waiting for Kafka..."
  sleep 2
done
echo "Kafka is ready"

echo "Waiting for MongoDB..."
until nc -z localhost 27017; do
  echo "Waiting for MongoDB port 27017..."
  sleep 2
done
echo "MongoDB port is ready"

echo "=== Infrastructure ready ==="