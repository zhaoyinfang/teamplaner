# ---- Stage 1: bauen ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Zuerst nur die pom.xml: Abhaengigkeiten werden gecacht, solange sich die pom nicht aendert
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Stage 2: laufen ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /build/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
