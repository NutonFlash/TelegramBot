FROM openjdk:17
COPY ./target/HQDTelegramBot-0.0.1-SNAPSHOT.jar HQDTelegramBot-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","HQDTelegramBot-0.0.1-SNAPSHOT.jar"]