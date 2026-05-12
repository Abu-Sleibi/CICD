# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache dependency layer separately so rebuilds skip mvn download
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# curl is needed for the ECS container health check
RUN apk add --no-cache curl \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup

USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
