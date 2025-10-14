FROM openjdk:17-slim
ARG JAR_FILE=target/image-service.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]