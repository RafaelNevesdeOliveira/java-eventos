FROM docker.io/library/maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY ordens-domain/pom.xml ordens-domain/pom.xml
COPY ordens-domain/src ordens-domain/src
COPY aula1/pom.xml aula1/pom.xml
COPY aula2/pom.xml aula2/pom.xml
COPY aula2/src aula2/src
RUN mvn -B -pl aula2 -am package -DskipTests
FROM docker.io/library/eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/aula2/target/aula2-1.0.0.jar app.jar
USER 10001
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65", "-jar", "/app/app.jar"]
