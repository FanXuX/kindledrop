# Build stage
FROM gradle:8.8.0-jdk21-jammy AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
WORKDIR /home/gradle/src/services/engine
RUN gradle build --no-daemon

# Package stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/services/engine/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
