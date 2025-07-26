# Use a slim JDK 21 image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built Spring Boot JAR from your target/ directory to /app/teryaq.jar in the container
COPY target/Teryaq-0.0.1-SNAPSHOT.jar /app/teryaq.jar

# Expose the port your Spring Boot application listens on (3000)
EXPOSE 3000

# Define the command to run your Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/teryaq.jar"]