# Sử dụng image Java 17
FROM eclipse-temurin:17-jdk-alpine
# Đặt thư mục làm việc
WORKDIR /app
# Copy file .jar đã build
COPY target/prosper-path-0.0.1-SNAPSHOT.jar app.jar
# Chạy ứng dụng với profile prod
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]