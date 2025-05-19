# ----------- Build Stage -----------
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /tmp/app

# Copy Gradle wrapper and config files first to leverage layer caching
COPY gradlew settings.gradle build.gradle gradle/ ./

# Make gradlew executable
RUN chmod +x gradlew

# Run a Gradle task to download dependencies and verify wrapper works
RUN ./gradlew --no-daemon help

# Copy the rest of the source code
COPY . .

# Build your project skipping tests to speed up
RUN ./gradlew clean build -x test --parallel --no-daemon --stacktrace

# ----------- Runtime Stage -----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the fat jar from builder stage
COPY --from=builder /tmp/app/build/libs/*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh .

# Make entrypoint executable
RUN chmod +x entrypoint.sh

# Install tini and bash for proper process handling and shell support
RUN apt-get update && apt-get install -y --no-install-recommends tini bash \
    && ln -sf /usr/bin/tini /sbin/tini \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--", "./entrypoint.sh"]
