#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

SERVICES=("AuthenticationService" "ImageService" "ActivityService" "APIGatewayService")
JAR_FILES=("authentication-service.jar" "image-service.jar" "activity-service.jar" "api-gateway-service.jar")

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

NEED_BUILD=false

if [ ! -f "Common/target/common.jar" ]; then
  echo "=== Common module needs rebuild ==="
  NEED_BUILD=true
else
  if [ -d "Common/target/classes" ]; then
    LAST_BUILD=$(stat -f "%m" "Common/target/common.jar" 2>/dev/null || echo 0)
    LAST_SOURCE=$(find "Common/src" -name "*.java" -newer "Common/target/common.jar" -print -quit 2>/dev/null | wc -l)
    if [ "$LAST_SOURCE" -gt 0 ]; then
      echo "=== Common module source changed, needs rebuild ==="
      NEED_BUILD=true
    fi
  else
    echo "=== Common module classes not found, needs rebuild ==="
    NEED_BUILD=true
  fi
fi

SERVICES_CHANGED=false
for i in "${!SERVICES[@]}"; do
  service="${SERVICES[$i]}"
  jar_file="${JAR_FILES[$i]}"

  if [ ! -f "$service/target/$jar_file" ]; then
    echo "=== $service needs rebuild ==="
    SERVICES_CHANGED=true
  else
    if [ -f "Common/target/common.jar" ]; then
      COMMON_BUILD_TIME=$(stat -f "%m" "Common/target/common.jar" 2>/dev/null || echo 0)
      SERVICE_BUILD_TIME=$(stat -f "%m" "$service/target/$jar_file" 2>/dev/null || echo 0)
      if [ "$COMMON_BUILD_TIME" -gt "$SERVICE_BUILD_TIME" ]; then
        echo "=== $service needs rebuild (Common changed) ==="
        SERVICES_CHANGED=true
      fi
    fi

    if [ -d "$service/src" ]; then
      LAST_SERVICE_BUILD=$(stat -f "%m" "$service/target/$jar_file" 2>/dev/null || echo 0)
      LAST_SERVICE_SOURCE=$(find "$service/src" -name "*.java" -o -name "*.kt" -newer "$service/target/$jar_file" -print -quit 2>/dev/null | wc -l)
      if [ "$LAST_SERVICE_SOURCE" -gt 0 ]; then
        echo "=== $service source changed, needs rebuild ==="
        SERVICES_CHANGED=true
      fi
    fi
  fi
done

if [ "$NEED_BUILD" = true ] || [ "$SERVICES_CHANGED" = true ]; then
  if [ "$NEED_BUILD" = true ]; then
    echo "=== Building Common module ==="
    mvn -T 4 -pl Common clean install -DskipTests --fail-fast
  fi

  echo "=== Building all services ==="
  mvn -T 4 -pl AuthenticationService,ImageService,ActivityService,APIGatewayService package -DskipTests --fail-fast
else
  echo "=== All modules are up to date, skipping build ==="
fi

echo "=== Stopping existing containers (keeping volumes) ==="
docker compose stop

echo "=== Starting all services ==="
docker compose up -d

echo "=== Waiting for services to be ready ==="
echo "Waiting for infrastructure services..."
until docker compose exec postgres pg_isready -U postgres >/dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done
echo "PostgreSQL is ready"

until docker compose exec localstack awslocal s3 ls >/dev/null 2>&1; do
  echo "Waiting for LocalStack..."
  sleep 2
done

if ! docker compose exec localstack awslocal s3api head-bucket --bucket images >/dev/null 2>&1; then
  echo "Creating S3 bucket 'images'"
  docker compose exec localstack awslocal s3 mb s3://images
fi

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

echo "=== All services started successfully ==="
echo "Frontend: http://localhost:3000"
echo "API Gateway: http://localhost:8085"
echo "Authentication Service: http://localhost:8080"
echo "Image Service: http://localhost:8081"
echo "Activity Service: http://localhost:8082"
