FROM openjdk:17-jdk-alpine
COPY ./target/*.jar xeramed-backend.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/xeramed-backend.jar"]