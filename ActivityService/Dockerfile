FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/activity-service.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]