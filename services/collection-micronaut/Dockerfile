# syntax=docker/dockerfile:1

# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null

COPY src src
# Tests need Docker (Testcontainers); run them with `./gradlew test` outside the image build.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon installDist -x test

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system pokeapp && useradd --system --gid pokeapp pokeapp
WORKDIR /app
COPY --from=build /workspace/build/install/collection-micronaut /app
USER pokeapp

EXPOSE 8083
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=10 \
    CMD curl -fs http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["/app/bin/collection-micronaut"]
