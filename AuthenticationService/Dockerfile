# Use official OpenJDK 17 slim image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/AuthenticationService-1.0.0.jar app.jar

# Set default environment variables
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=innowise
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]