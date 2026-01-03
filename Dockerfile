FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]