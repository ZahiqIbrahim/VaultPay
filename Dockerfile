FROM amazoncorretto:21-alpine-jdk
COPY target/vaultpay-app.jar vaultpay-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "vaultpay-app.jar"]