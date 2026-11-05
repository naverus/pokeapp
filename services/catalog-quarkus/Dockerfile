# syntax=docker/dockerfile:1

# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null

COPY src src
# Tests start an in-process PokeAPI stub; run them with `./gradlew test` outside the image build.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon quarkusBuild -x test \
    && cp -r build/quarkus-app /workspace/quarkus-app

# ---- runtime stage (JVM mode, fast-jar layout) ----
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system pokeapp && useradd --system --gid pokeapp pokeapp
WORKDIR /app
COPY --from=build --chown=pokeapp:pokeapp /workspace/quarkus-app/ /app/
USER pokeapp

EXPOSE 8082
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75" \
    POKEAPP_POKEAPI_BASE_URL="https://pokeapi.co/api/v2"
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=10 \
    CMD curl -fs http://localhost:8082/q/health/ready || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.util.logging.manager=org.jboss.logmanager.LogManager -jar /app/quarkus-run.jar"]
