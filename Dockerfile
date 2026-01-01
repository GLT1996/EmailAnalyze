# 第一阶段：使用Maven镜像构建
FROM maven:3.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# 复制源代码
COPY src ./src
# 打包，跳过测试以加速构建
RUN mvn clean package -DskipTests

# 第二阶段：使用轻量级JRE运行
FROM eclipse-temurin:21-jre
WORKDIR /app
# 从构建阶段复制生成的JAR包
COPY --from=builder /app/target/*.jar app.jar
# 应用应监听Render注入的$PORT环境变量
ENTRYPOINT ["java", "-jar", "app.jar"]