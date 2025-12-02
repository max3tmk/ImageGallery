#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

launch_port_forward_in_new_terminal() {
  if [[ "$OSTYPE" == "darwin"* ]]; then
    osascript -e 'tell app "Terminal"
      do script "cd \"'"$REPO_ROOT"'\" && ./scripts/port-forward.sh"
    end tell' >/dev/null 2>&1 &
    echo "New Terminal window opened with port-forwards."

  elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    if command -v gnome-terminal >/dev/null; then
      gnome-terminal --working-directory="$REPO_ROOT" -- bash -c "./scripts/port-forward.sh; exec bash" &
      echo "GNOME Terminal opened with port-forwards."
    elif command -v xterm >/dev/null; then
      xterm -hold -e "cd '$REPO_ROOT' && ./scripts/port-forward.sh" &
      echo "xterm opened with port-forwards."
    elif command -v konsole >/dev/null; then
      konsole --hold -e bash -c "cd '$REPO_ROOT' && ./scripts/port-forward.sh" &
      echo "Konsole opened with port-forwards."
    else
      echo "No known terminal found. Run manually: ./scripts/port-forward.sh"
    fi

  else
    echo "Unsupported OS ($OSTYPE). Run manually: ./scripts/port-forward.sh"
  fi
}

echo "Starting Minikube..."
minikube start --driver=docker --memory=6144 --cpus=4

echo "Switching to Minikube Docker..."
eval $(minikube docker-env)

echo "Preloading infrastructure images..."
docker pull localstack/localstack:0.14.2
docker pull apache/kafka:3.8.0
docker pull postgres:15
docker pull mongo:6.0

echo "Building ALL service images (forced for reliability)..."
( cd AuthenticationService && mvn -T 4 package -DskipTests --fail-fast )
docker build -t authentication-service:latest AuthenticationService/

( cd ImageService && mvn -T 4 package -DskipTests --fail-fast )
docker build -t image-service:latest ImageService/

( cd ActivityService && mvn -T 4 package -DskipTests --fail-fast )
docker build -t activity-service:latest ActivityService/

( cd APIGatewayService && mvn -T 4 package -DskipTests --fail-fast )
docker build -t api-gateway:latest APIGatewayService/

( cd frontend && npm ci && npm run build )
docker build -t frontend:latest frontend/

echo "Applying manifests..."
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/config/
kubectl apply -f k8s/databases/postgres/
kubectl apply -f k8s/databases/mongo/
kubectl apply -f k8s/infrastructure/kafka/
kubectl apply -f k8s/infrastructure/localstack/
kubectl apply -f k8s/services/authentication/
kubectl apply -f k8s/services/image/
kubectl apply -f k8s/services/activity/
kubectl apply -f k8s/services/gateway/
kubectl apply -f k8s/services/frontend/

echo "Restarting services..."
kubectl rollout restart deployment authentication-service
kubectl rollout restart deployment image-service
kubectl rollout restart deployment activity-service
kubectl rollout restart deployment api-gateway
kubectl rollout restart deployment frontend

echo "Waiting for infrastructure..."

echo -n "PostgreSQL... "
while ! kubectl exec -it deployment/postgres -- pg_isready -U postgres >/dev/null 2>&1; do sleep 3; echo -n "."; done; echo " DONE"

echo -n "MongoDB... "
while ! kubectl exec -it deployment/mongo -- mongosh --eval 'db.runCommand({ping:1})' >/dev/null 2>&1; do sleep 3; echo -n "."; done; echo " DONE"

echo -n "LocalStack... "
while ! kubectl exec -it deployment/localstack -- curl -sf http://localhost:4566/health >/dev/null 2>&1; do sleep 5; echo -n "."; done; echo " DONE"

echo -n "Kafka... "
while ! kubectl exec -it deployment/kafka -- /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; do sleep 5; echo -n "."; done; echo " DONE"

echo "System is ready!"
echo "Frontend: http://localhost:3000"
echo "API Gateway: http://localhost:8085"

echo "Launching port-forward in a new terminal..."
launch_port_forward_in_new_terminal

echo "To stop everything: make k8s-stop"