# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- run ---
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 app && mkdir -p /data/files && chown -R app /data
COPY --from=build /src/target/yapi365-*.jar app.jar
USER app
ENV STORAGE_DIR=/data/files
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
