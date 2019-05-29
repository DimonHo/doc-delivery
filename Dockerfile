FROM openjdk:8-jdk-alpine

RUN mkdir /app && mkdir /app/catche
WORKDIR /app
COPY target/doc-delivery.jar /app/app.jar
EXPOSE 80
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar","/app/app.jar"]
