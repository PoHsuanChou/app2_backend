## 使用 Maven 构建阶段
#FROM maven:3.8.4-openjdk-17 as builder
#
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#
## 执行 Maven 构建
#RUN mvn clean package -DskipTests
#
## 运行阶段
#FROM openjdk:17-jdk-slim
#
## 安裝 wget 用於健康檢查
#RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
#
#WORKDIR /app
#
## 从构建阶段复制构建好的 JAR 文件
#COPY --from=builder /app/target/app2_backend-1.0-SNAPSHOT.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java","-jar","app.jar"]


# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Install wait-for-it script to handle service dependencies
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose port
EXPOSE 8080

# Use environment variables for Redis and MongoDB hosts
ENTRYPOINT ["/wait-for-it.sh", "mongodb:27017", "--", "/wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]