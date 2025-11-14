FROM eclipse-temurin:17-jre-alpine AS base
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/activity-service.jar app.jar
USER root
RUN apk update && apk add --no-cache curl
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]