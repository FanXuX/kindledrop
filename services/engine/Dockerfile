# Build stage
FROM gradle:9-jdk21-noble AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN ls -la
RUN gradle build --no-daemon

# Package stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
