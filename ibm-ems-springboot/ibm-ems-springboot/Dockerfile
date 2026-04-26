# =============================================================================
# IBM EMS - Multi-Stage Dockerfile
# Stage 1: Build with Maven + JDK 17
# Stage 2: Run with JRE-only (smaller image)
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /workspace

# Cache dependency layer — only re-downloads if pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime (JRE only) ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="IBM EMS API" \
      org.opencontainers.image.description="IBM Employee Management System" \
      org.opencontainers.image.vendor="State Bank of India" \
      org.opencontainers.image.version="1.0.0"

# Run as a dedicated non-root user
RUN addgroup -S emsgroup && adduser -S emsuser -G emsgroup

WORKDIR /app

COPY --from=build /workspace/target/ems-*.jar app.jar

RUN mkdir -p /app/logs && chown -R emsuser:emsgroup /app

USER emsuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# JVM tuned for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
