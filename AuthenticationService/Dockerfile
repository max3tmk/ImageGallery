FROM openjdk:17-slim

WORKDIR /app

COPY target/authentication-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
