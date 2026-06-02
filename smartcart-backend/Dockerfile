# ===================================================================
# Stage 1: Build with Maven + JDK 25
# ===================================================================
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Install curl and tar to download and install Maven manually (avoids openjdk25-jmods package extraction error)
RUN apk add --no-cache curl tar

# Download and install Apache Maven 3.9.9
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xzC /usr/share \
    && ln -s /usr/share/apache-maven-3.9.9/bin/mvn /usr/bin/mvn

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ===================================================================
# Stage 2: Lightweight Runtime with JRE 25
# ===================================================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S smartcart && adduser -S smartcart -G smartcart

# Create writable runtime directories
RUN mkdir -p /app/uploads /app/logs && chown -R smartcart:smartcart /app/uploads /app/logs

COPY --from=build /app/target/*.jar app.jar
RUN chown smartcart:smartcart app.jar

USER smartcart

EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
