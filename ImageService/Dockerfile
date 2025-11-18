FROM eclipse-temurin:17-jre-alpine AS base
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY image-service.jar jar1.jar
COPY target/image-service.jar jar2.jar
RUN cp jar1.jar app.jar 2>/dev/null || cp jar2.jar app.jar
USER root
RUN apk update && apk add --no-cache curl
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]