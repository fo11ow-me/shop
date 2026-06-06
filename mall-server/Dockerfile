FROM eclipse-temurin:17-jre-jammy
COPY target/mall-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8800
ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app.jar"]
