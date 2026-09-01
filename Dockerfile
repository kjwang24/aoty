# syntax=docker/dockerfile:1

# build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# dependencies, only change when pom.xml changes
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
# B tells mvnw to fail automatically instead of waiting for user input
RUN ./mvnw -B dependency:go-offline
COPY src/ src/
COPY frontend/ frontend/

# runtime
FROM eclipse=temurin:21-jre-alpine
WORKDIR /app
RUN adduser -D -H app
USER app
COPY --from=build /src/target/aoty-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]