#!/bin/bash

CONTAINER_NAME="localstack"
LOCALSTACK_VERSION="2.3.2"  # latest or 2.3.2
BUCKET_NAME="images"

echo ""
echo "Starting LocalStack (S3 emulator) in Docker..."
echo ""
echo "Using LocalStack version: $LOCALSTACK_VERSION"

# Останавливаем и удаляем существующий контейнер
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "Stopping and removing existing LocalStack container..."
    docker rm -f $CONTAINER_NAME
fi

# Запуск контейнера БЕЗ монтирования /tmp/localstack
docker run -d --name $CONTAINER_NAME -p 4566:4566 localstack/localstack:$LOCALSTACK_VERSION

# Ждём пока LocalStack станет готов
echo "Waiting for LocalStack S3 to be ready..."
MAX_WAIT=60
for i in $(seq 1 $MAX_WAIT); do
    docker exec $CONTAINER_NAME awslocal s3 ls &> /dev/null && break
    sleep 1
done

if [ $i -eq $MAX_WAIT ]; then
    echo "S3 is not responding after $MAX_WAIT seconds. Check logs: docker logs $CONTAINER_NAME"
    exit 1
fi

echo "LocalStack S3 is ready."

# Создаём бакет
echo "Creating bucket '$BUCKET_NAME'..."
docker exec $CONTAINER_NAME awslocal s3 mb s3://$BUCKET_NAME

# Ждём 3 секунды для надёжности
sleep 3

# Создаём и загружаем тестовый файл ВНУТРИ контейнера
echo "Uploading test file..."
docker exec $CONTAINER_NAME sh -c "echo 'This is a test file' > /tmp/testfile.txt"
if docker exec $CONTAINER_NAME awslocal s3 cp /tmp/testfile.txt s3://$BUCKET_NAME/testfile.txt; then
    echo "File uploaded successfully."
else
    echo "File upload failed."
    exit 1
fi

# Удаляем временный файл внутри контейнера
docker exec $CONTAINER_NAME rm -f /tmp/testfile.txt

echo ""
echo "LocalStack is fully ready for use."
echo ""
