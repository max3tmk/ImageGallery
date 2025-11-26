FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/ ./from_target/
COPY . ./from_root/
RUN \
  if [ -f from_root/image-service.jar ]; then \
    cp from_root/image-service.jar app.jar; \
  elif [ -f from_target/image-service.jar ]; then \
    cp from_target/image-service.jar app.jar; \
  else \
    echo "JAR not found" && exit 1; \
  fi
USER root
RUN apk update && apk add --no-cache curl
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]