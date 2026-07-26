FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=target/*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} app.jar

EXPOSE 9788

ENTRYPOINT ["java", "-jar", "app.jar"]