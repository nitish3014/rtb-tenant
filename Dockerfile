# ----------- Build Stage -----------
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /tmp/app

# Copy gradlew and gradle wrapper files explicitly
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties

# Copy main Gradle config files
COPY settings.gradle .
COPY build.gradle .

RUN chmod +x gradlew

# Run Gradle help to download dependencies & verify wrapper
RUN ./gradlew --no-daemon help

# Copy rest of the source code
COPY src ./src

# Build the application skipping tests for speed
RUN ./gradlew clean build -x test --parallel --no-daemon --stacktrace

# ----------- Runtime Stage -----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the fat jar from builder stage
COPY --from=builder /tmp/app/build/libs/*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

# Install tini and bash for proper process handling
RUN apt-get update && apt-get install -y --no-install-recommends tini bash \
    && ln -sf /usr/bin/tini /sbin/tini \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--", "./entrypoint.sh"]
