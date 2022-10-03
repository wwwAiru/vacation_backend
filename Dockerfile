FROM maven:3.8.3-openjdk-17
COPY target/vacation-backend-0.0.1-SNAPSHOT.jar /app/vacation_backend.jar
ENTRYPOINT java -jar /app/vacation_backend.jar
