# syntax=docker/dockerfile:1.6

########################################
# Stage 1: Build
########################################
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy only files needed to resolve dependencies first
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies

# Copy source last (invalidates cache only when code changes)
COPY src src

# Build the application
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon build -x test


########################################
# Stage 2: Runtime
########################################
FROM eclipse-temurin:17-jre-alpine

# Install dumb-init
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy built jar
COPY --from=build /app/build/libs/*.jar app.jar

# Fix ownership
RUN chown -R spring:spring /app

USER spring

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-Xmx512m", "-jar", "app.jar"]
