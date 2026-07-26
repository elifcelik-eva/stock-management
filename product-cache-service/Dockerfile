FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

ARG JAR_FILE=target/*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} app.jar

EXPOSE 9791

ENTRYPOINT ["java", "-jar", "app.jar"]