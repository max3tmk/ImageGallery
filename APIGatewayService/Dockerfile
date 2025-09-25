FROM openjdk:17-slim

WORKDIR /app

COPY target/APIGatewayService-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]