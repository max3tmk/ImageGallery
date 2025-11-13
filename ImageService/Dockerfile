FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/image-service.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]