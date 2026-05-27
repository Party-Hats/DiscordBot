FROM maven:3.9-eclipse-temurin-21 as builder
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM eclipse-temurin:21-jre

USER root
WORKDIR /mnt/bin

COPY --from=builder /home/app/target/TwitchBot.jar /mnt/bin

ENTRYPOINT ["java", "-Dspring.profiles.active=container", "-jar", "/mnt/bin/TwitchBot.jar"]