FROM eclipse-temurin:24-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:24-jre

WORKDIR /app

RUN groupadd --system instagram && useradd --system --gid instagram instagram

COPY --from=build /workspace/build/libs/*.jar app.jar

USER instagram
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
