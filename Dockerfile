# Use official Eclipse Temurin JDK 17 for build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /tmp/app

# Copy only Gradle wrapper and config files first to cache dependencies
COPY gradlew settings.gradle build.gradle gradle/ ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon help

# Copy rest of the source code
COPY . .

# Build the app, skipping tests for faster builds
RUN ./gradlew clean build -x test --parallel --no-daemon --stacktrace

# Use official Eclipse Temurin JRE 17 for runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /tmp/app/build/libs/*.jar app.jar

# Copy your entrypoint script
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

# Install tini and bash for proper signal handling and shell support
RUN apt-get update && apt-get install -y --no-install-recommends tini bash \
    && ln -sf /usr/bin/tini /sbin/tini \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--", "./entrypoint.sh"]
