#!/bin/bash
set -e

echo "Waiting for ALL services to be ready..."

wait_for_service() {
  local service="$1"
  local port="$2"
  echo -n "  $service... "
  while ! kubectl get pod -l app="$service" -o 'jsonpath={.items[*].status.conditions[?(@.type=="Ready")].status}' 2>/dev/null | grep -q "True"; do
    sleep 2
    echo -n "."
  done
  echo " DONE"
}

wait_for_service "authentication-service" 8080
wait_for_service "image-service" 8081
wait_for_service "activity-service" 8082
wait_for_service "api-gateway" 8085
wait_for_service "frontend" 80
wait_for_service "postgres" 5432
wait_for_service "mongo" 27017
wait_for_service "localstack" 4566
wait_for_service "kafka" 9092

echo ""
echo "Starting port-forwards..."
kubectl port-forward service/postgres-service 5432:5432 &
kubectl port-forward service/mongo-service 27017:27017 &
kubectl port-forward service/localstack 4566:4566 &
kubectl port-forward service/kafka-service 9092:9092 &
kubectl port-forward service/frontend 3000:80 &
kubectl port-forward service/api-gateway 8085:8085 &

echo "All port-forwards are active."
echo "Press Ctrl+C to stop."
wait