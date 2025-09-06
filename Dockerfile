# Multi-stage Dockerfile for Teryaq Spring Boot application with Python support
# دعم Python للنظام الصيدلاني

# Stage 1: Build stage with Maven
FROM maven:3.9.6 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy the source code
COPY src ./src

# Build the application with Maven
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with Python support
FROM openjdk:21-jdk-slim

# Install Python and required system packages
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    python3-dev \
    build-essential \
    libpq-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create symbolic link for python command
RUN ln -s /usr/bin/python3 /usr/bin/python

# Set the working directory inside the container
WORKDIR /app

# Create directories for scripts and logs
RUN mkdir -p /app/scripts /app/logs /tmp/pharmaceutical

# Copy Python scripts and requirements
COPY scripts/ /app/scripts/

# Install Python dependencies
RUN if [ -f /app/scripts/requirements.txt ]; then \
        pip3 install --no-cache-dir -r /app/scripts/requirements.txt; \
    fi

# Make Python scripts executable
RUN chmod +x /app/scripts/*.py

# Copy the built Spring Boot JAR from the build stage
COPY --from=build /app/target/Teryaq-0.0.1-SNAPSHOT.jar /app/teryaq.jar

# Create non-root user for security
RUN groupadd -r teryaq && useradd -r -g teryaq teryaq
RUN chown -R teryaq:teryaq /app /tmp/pharmaceutical
USER teryaq

# Expose the port your Spring Boot application listens on (3000)
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:3000/api/pharmaceutical/health || exit 1

# Define the command to run your Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/teryaq.jar"]

