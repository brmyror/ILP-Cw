FROM maven:3.9.6-amazoncorretto-21-debian AS build
# Set the working directory inside the container
WORKDIR /app
# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src
# Package the application
RUN mvn clean package -DskipTests
# Use an offical OpenJDK image as the base image
FROM eclipse-temurin:21-jdk
# Set the working directory inside the container
WORKDIR /app
# Copy the built jar file from the previous stage to the container
COPY --from=build /app/target/*.jar ./app.jar
# Expose the application port (this declares the container port only)
# Note: Dockerfile cannot force the host port mapping. To bind host port 8080 to container port 8080
# run the container with: docker run -p 8080:8080 ilp_submission_image:latest
EXPOSE 8080
# Set the environment variable for the ILP endpoint (typo-corrected name)
ENV ILP_ENDPOINT=https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/
# Run the application
CMD ["java", "-jar", "./app.jar"]