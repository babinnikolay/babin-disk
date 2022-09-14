FROM amazoncorretto:11-alpine-jdk
COPY target/app.jar docker/app.jar
WORKDIR docker
ENTRYPOINT ["java", "-jar", "app.jar"]