# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies first (cached layer)
RUN ./mvnw dependency:go-offline -q

COPY src src

RUN ./mvnw package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S psms && adduser -S psms -G psms

COPY --from=builder /app/target/*.jar app.jar

RUN chown psms:psms app.jar
USER psms

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
