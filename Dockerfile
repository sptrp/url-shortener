FROM gradle:9.1.0-jdk17 AS build

WORKDIR /app

COPY . .

RUN chown -R gradle:gradle /app

USER gradle

RUN gradle clean shadowJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]