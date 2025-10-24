FROM openjdk:17-slim

WORKDIR /app

COPY target/authentication-service.jar app.jar

ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=innowise
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innowise

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
