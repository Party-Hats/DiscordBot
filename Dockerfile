FROM maven:3.6.3-openjdk-15-slim as builder
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:14

USER root
WORKDIR /mnt/bin

COPY --from=builder /home/app/target/TwitchBot.jar /mnt/bin

ENTRYPOINT ["java", "-Dspring.profiles.active=container", "-jar", "/mnt/bin/TwitchBot.jar"]