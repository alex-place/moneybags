# syntax=docker/dockerfile:1.6

############################
# Build stage
############################
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Gradle wrapper + config first
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Cache Gradle dependencies
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies

# Copy source last
COPY src src

# Build (skip tests for speed)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon build -x test


############################
# Runtime stage
############################
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache dumb-init

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown -R spring:spring /app
USER spring

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-Xms64m", "-Xmx256m", "-jar", "app.jar"]
