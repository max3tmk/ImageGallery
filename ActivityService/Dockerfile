FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/ ./from_target/
COPY . ./from_root/
RUN \
  if [ -f from_root/activity-service.jar ]; then \
    cp from_root/activity-service.jar app.jar; \
  elif [ -f from_target/activity-service.jar ]; then \
    cp from_target/activity-service.jar app.jar; \
  else \
    echo "JAR not found" && exit 1; \
  fi
USER root
RUN apk update && apk add --no-cache curl
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]