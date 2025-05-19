# -------- Build Stage ----------
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /tmp/app

COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY settings.gradle .
COPY build.gradle .
RUN chmod +x gradlew
RUN ./gradlew --no-daemon help

COPY src ./src

RUN ./gradlew clean build -x test --parallel --no-daemon --stacktrace

# -------- Runtime Stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /tmp/app/build/libs/*.jar app.jar

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

RUN apt-get update && apt-get install -y --no-install-recommends tini bash \
    && ln -sf /usr/bin/tini /sbin/tini \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--", "./entrypoint.sh"]
