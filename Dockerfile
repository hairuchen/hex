# 第一阶段：编译打包（使用官方 Maven 镜像，更专业）
# 这个镜像已经内置了 JDK 21 和 Maven，无需手动安装
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 复制 Maven 配置文件和项目文件
# 这样可以利用 Docker 的层缓存，只有 pom.xml 或 src 变化时才会重新下载依赖和编译
COPY pom.xml .
COPY src ./src
COPY entrypoint.sh .

# 配置 Maven 使用阿里云镜像源，加速依赖下载
# 将 settings.xml 文件复制到容器内的 Maven 配置目录
RUN echo "<?xml version='1.0' encoding='UTF-8'?><settings xmlns='http://maven.apache.org/SETTINGS/1.2.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd'><mirrors><mirror><id>aliyunmaven</id><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > /usr/share/maven/conf/settings.xml

# 打包项目
RUN mvn clean package spring-boot:repackage -Dmaven.test.skip=true

# 第二阶段：运行（使用你指定的 Amazon Corretto 21 镜像）
# 这个镜像非常小，适合作为最终的运行环境
FROM amazoncorretto:21.0.9-al2023
WORKDIR /app

# 从构建阶段复制 JAR 包和启动脚本
# --from=builder 指定从第一个阶段（builder）复制文件
COPY --from=builder /app/target/hex-alpha.jar /app/app.jar
COPY --from=builder /app/entrypoint.sh /app/entrypoint.sh

# 赋予启动脚本执行权限
RUN chmod +x /app/entrypoint.sh

# 暴露应用端口
EXPOSE 8080

# 容器启动命令
ENTRYPOINT ["/app/entrypoint.sh"]