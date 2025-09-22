#!/bin/bash

echo "Starting PostgreSQL container..."

# Stop and delete existing container if present
docker stop postgres-app 2>/dev/null && docker rm postgres-app 2>/dev/null

# Starting new PostgreSQL container
docker run -d \
  --name postgres-app \
  -e POSTGRES_DB=innowise \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

echo "postgres-app:5432 is running."
