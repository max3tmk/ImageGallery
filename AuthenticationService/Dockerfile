FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/authentication-service.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]