FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests=true

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/target/ms-credits-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
