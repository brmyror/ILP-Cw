FROM maven:3.9.6-amazoncorretto-21-debian AS build
# Set the working directory inside the container
WORKDIR /app
# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src
# Package the application
RUN mvn clean package -DskipTests
# Use an offical OpenJDK image as the base image
FROM openjdk:23
# Set the working directory inside the container
WORKDIR /app
# Copy the built jar file from the previous stage to the container
COPY --from=build /app/target/*.jar app.jar
# Expose the application port
EXPOSE 8080
# Set the entry point to run the application
ENV ILP_ENPOINT=https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/
CMD ["java", "-jar", "./app.jar"]