FROM maven:3.8.3-adoptopenjdk-15 as build

WORKDIR staging

COPY ./pom.xml .

COPY ./client/src client/src
COPY ./client/pom.xml client

COPY ./server/src server/src
COPY ./server/pom.xml server

COPY ./core/src core/src
COPY ./core/pom.xml core

RUN mvn clean package

FROM openjdk:15-alpine3.12

WORKDIR app
RUN mkdir data
COPY --from=build /staging/server/target/cloud-server.jar cloud-server.jar

EXPOSE 8190

CMD ["java", "-jar", "cloud-server.jar"]