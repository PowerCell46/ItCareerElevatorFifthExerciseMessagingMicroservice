FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install maven
RUN apk add --no-cache maven

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]