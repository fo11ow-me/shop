# 构建阶段：在镜像内完成 Maven 打包，本地无需预装 Java/Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# 运行阶段
FROM eclipse-temurin:17-jre-jammy
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8800
ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app.jar"]
