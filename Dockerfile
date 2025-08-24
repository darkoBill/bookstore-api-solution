FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

RUN groupadd -r bookstore && useradd -r -g bookstore bookstore

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/bookstore-api.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
    -XX:InitialRAMPercentage=50 \
    -XX:MinRAMPercentage=50 \
    -XX:MaxRAMPercentage=75 \
    -XX:+AlwaysPreTouch \
    -XX:+UseStringDeduplication \
    -XX:MaxGCPauseMillis=200 \
    -Xlog:gc*,safepoint:file=/tmp/jvm-gc.log:time,uptime,tid,level,tags \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -XX:+ExitOnOutOfMemoryError"

USER bookstore

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]