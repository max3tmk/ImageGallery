FROM openjdk:17-slim

WORKDIR /app

COPY target/api-gateway-service.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
