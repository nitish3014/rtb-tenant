FROM eclipse-temurin:17-jdk-slim AS builder
WORKDIR /build

COPY gradlew settings.gradle build.gradle gradle/ ./
RUN chmod +x gradlew \
    && ./gradlew --no-daemon help --build-cache

COPY . .
RUN ./gradlew clean build -x test --parallel --stacktrace --no-daemon


FROM eclipse-temurin:17-jre-slim
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

RUN apt-get update \
 && apt-get install -y --no-install-recommends tini bash \
 && ln -sf /usr/bin/tini /sbin/tini \
 && rm -rf /var/lib/apt/lists/*


COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/sbin/tini", "--", "./entrypoint.sh"]
