# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src/ ./src/

# Swap JDBC host so the WAR built inside Docker points at the postgres service
RUN sed -i 's#jdbc:postgresql://localhost:5432/tournament#jdbc:postgresql://postgres:5432/tournament#' \
    src/main/resources/META-INF/persistence.xml

RUN mvn -B -q clean package -DskipTests

FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/Tournament-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
