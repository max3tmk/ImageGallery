FROM openjdk:17-slim

RUN adduser --no-create-home --disabled-password --gecos '' appuser

ARG JAR_FILE=target/*.jar
WORKDIR /app

COPY ${JAR_FILE} app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]