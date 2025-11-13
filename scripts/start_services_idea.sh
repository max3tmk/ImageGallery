#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

SERVICES=("AuthenticationService" "ImageService" "ActivityService" "APIGatewayService")
JAR_FILES=("authentication-service.jar" "image-service.jar" "activity-service.jar" "api-gateway-service.jar")

PORTS_SERVICES="8080:authentication-service 8081:image-service 8082:activity-service 8085:api-gateway 3000:frontend"

echo "=== Checking and killing processes on service ports ==="
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

echo "=== Checking if infrastructure is running ==="
INFRA_READY=true

if ! nc -z localhost 5432 2>/dev/null; then
  echo "PostgreSQL is not running on port 5432"
  INFRA_READY=false
fi

if ! nc -z localhost 4566 2>/dev/null; then
  echo "LocalStack is not running on port 4566"
  INFRA_READY=false
fi

if ! nc -z localhost 9092 2>/dev/null; then
  echo "Kafka is not running on port 9092"
  INFRA_READY=false
fi

if ! nc -z localhost 27017 2>/dev/null; then
  echo "MongoDB is not running on port 27017"
  INFRA_READY=false
fi

if ! nc -z localhost 2181 2>/dev/null; then
  echo "Zookeeper is not running on port 2181"
  INFRA_READY=false
fi

if [ "$INFRA_READY" = false ]; then
  echo "=== ERROR: Infrastructure is not running ==="
  echo "Please run 'make idea' first to start infrastructure"
  exit 1
fi

echo "=== Infrastructure is ready ==="

NEED_BUILD=false

if [ ! -f "Common/target/common.jar" ]; then
  echo "=== Common module needs build ==="
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
    echo "=== $service needs build ==="
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

  echo "=== Building backend services ==="
  mvn -T 4 -pl AuthenticationService,ImageService,ActivityService,APIGatewayService package -DskipTests --fail-fast
else
  echo "=== All backend modules are up to date ==="
fi

wait_for_service() {
  local port=$1
  local service_name=$2
  local timeout=${3:-60}

  echo "Waiting for $service_name to be ready on port $port..."
  local count=0
  while [ $count -lt $timeout ]; do
    if nc -z localhost $port 2>/dev/null; then
      echo "$service_name is ready!"
      return 0
    fi
    sleep 1
    count=$((count + 1))
  done
  echo "Timeout waiting for $service_name"
  return 1
}

echo "=== Starting backend services in background ==="

echo "Starting Authentication Service..."
java -jar AuthenticationService/target/authentication-service.jar > /tmp/auth-service.log 2>&1 &
AUTH_PID=$!
if ! wait_for_service 8080 "Authentication Service" 60; then
  echo "Failed to start Authentication Service"
  exit 1
fi

echo "Starting Image Service..."
java -jar ImageService/target/image-service.jar > /tmp/image-service.log 2>&1 &
IMAGE_PID=$!
if ! wait_for_service 8081 "Image Service" 60; then
  echo "Failed to start Image Service"
  exit 1
fi

echo "Starting Activity Service..."
java -jar ActivityService/target/activity-service.jar > /tmp/activity-service.log 2>&1 &
ACTIVITY_PID=$!
if ! wait_for_service 8082 "Activity Service" 60; then
  echo "Failed to start Activity Service"
  exit 1
fi

echo "Starting API Gateway..."
java -jar APIGatewayService/target/api-gateway-service.jar > /tmp/api-gateway.log 2>&1 &
GATEWAY_PID=$!
if ! wait_for_service 8085 "API Gateway" 60; then
  echo "Failed to start API Gateway"
  exit 1
fi

echo "=== Building and starting Frontend ==="
cd frontend

FRONTEND_NEEDS_BUILD=false
if [ ! -d "dist" ] || [ ! -f "dist/index.html" ]; then
  echo "Frontend needs build - no dist directory"
  FRONTEND_NEEDS_BUILD=true
else
  LAST_BUILD=$(stat -f "%m" "dist/index.html" 2>/dev/null || echo 0)
  LAST_SOURCE=$(find "src" -name "*.jsx" -o -name "*.js" -o -name "*.css" -newer "dist/index.html" -print -quit 2>/dev/null | wc -l)
  if [ "$LAST_SOURCE" -gt 0 ]; then
    echo "Frontend source changed, needs rebuild"
    FRONTEND_NEEDS_BUILD=true
  fi
fi

if [ "$FRONTEND_NEEDS_BUILD" = true ]; then
  echo "Building frontend..."
  npm run build > /tmp/frontend-build.log 2>&1
  echo "Frontend build completed"
fi

echo "Starting Frontend server..."
npx serve -s dist -l 3000 > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

if ! wait_for_service 3000 "Frontend" 30; then
  echo "Failed to start Frontend"
  exit 1
fi

echo "=== All services started successfully ==="
echo "Authentication Service PID: $AUTH_PID"
echo "Image Service PID: $IMAGE_PID"
echo "Activity Service PID: $ACTIVITY_PID"
echo "API Gateway PID: $GATEWAY_PID"
echo "Frontend PID: $FRONTEND_PID"
echo ""
echo "Service logs are available in /tmp/ directory"
echo "To stop all services, run:"
echo "kill $AUTH_PID $IMAGE_PID $ACTIVITY_PID $GATEWAY_PID $FRONTEND_PID"
echo ""
echo "=== Application is ready! ==="
echo "Frontend: http://localhost:3000"
echo "API Gateway: http://localhost:8085"
