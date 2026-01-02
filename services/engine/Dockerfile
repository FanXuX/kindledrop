# Use an official OpenJDK runtime as a parent image
FROM openjdk:25-ea-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from the build context
COPY build/libs/*.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
