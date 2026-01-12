# 1단계: 빌드 환경 (Build Stage)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# 의존성 파일을 먼저 복사하여 캐시 활용 (빌드 속도 최적화)
COPY pom.xml .
RUN mvn dependency:go-offline

# 소스 코드 복사 및 빌드 (테스트 제외)
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 환경 (Run Stage)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드 결과물(JAR)만 복사
COPY --from=build /app/target/*.jar app.jar
# 설정 파일(CSV) 복사
COPY device-inventory.csv .

# Netty(8003)와 Spring(8080) 포트 개방
EXPOSE 8003 8080

# 가비지 컬렉터(GC) 최적화 옵션 추가 (대규모 데이터 처리용)
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-XX:+UseG1GC", "-jar", "app.jar"]