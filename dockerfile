FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/bankcards-app.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]