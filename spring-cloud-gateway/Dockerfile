FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

ARG JAR_FILE=target/*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} app.jar

EXPOSE 8762

ENTRYPOINT ["java", "-jar", "app.jar"]