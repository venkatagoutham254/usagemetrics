FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/billable-metrics-service.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
