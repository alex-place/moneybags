# Stage 1: Build the application
FROM gradle:8.5-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy Gradle configuration files first to leverage Docker cache
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies - this layer will be cached unless build.gradle changes
RUN gradle dependencies --no-daemon

# Copy the source code
COPY src ./src

# Build the application
RUN gradle build -x test --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create a non-root user to run the application
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -s /bin/sh -D spring

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership of the application files
RUN chown -R spring:spring /app

# Switch to the non-root user
USER spring:spring

# Expose the port Spring Boot runs on
ENV PORT=8080
EXPOSE $PORT

# Use dumb-init to run the application
ENTRYPOINT ["dumb-init", "--"]

# Run the Spring Boot application
CMD ["java", "-Xmx512m", "-Dserver.port=${PORT}", "-jar", "app.jar"]
