FROM maven:3.8.3-openjdk-17
COPY target/vacation-backend.jar /app/vacation_backend.jar
ENTRYPOINT java -jar /app/vacation_backend.jar
