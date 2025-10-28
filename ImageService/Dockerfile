FROM openjdk:17-slim

WORKDIR /app

COPY target/image-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]