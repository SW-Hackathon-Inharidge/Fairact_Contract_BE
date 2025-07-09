# Base image (OpenJDK 17 이상 필요 - Spring Boot 3.x부터는 Java 17 이상 필요)
FROM eclipse-temurin:17-jdk-alpine

# 앱 디렉토리 생성
WORKDIR /app

# JAR 파일 복사
COPY build/libs/Fairact_Contract_BE-0.0.1-SNAPSHOT.jar app.jar

# 포트 지정 (필요에 따라)
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]