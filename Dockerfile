# 第一阶段：编译打包
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN echo "<?xml version='1.0' encoding='UTF-8'?><settings xmlns='http://maven.apache.org/SETTINGS/1.2.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd'><mirrors><mirror><id>aliyunmaven</id><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > /usr/share/maven/conf/settings.xml

RUN mvn clean package spring-boot:repackage -Dmaven.test.skip=true

# 第二阶段：运行
FROM amazoncorretto:21.0.9-al2023
WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]