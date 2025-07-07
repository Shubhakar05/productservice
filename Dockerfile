FROM openjdk:17
COPY target/*.jar app/app.jar
WORKDIR /app/
ENTRYPOINT ["java", "-jar", "app.jar"]
