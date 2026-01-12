# 🚀 MassFlux-Gateway (C# to Java Migration)

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-green?logo=springboot)
![Netty](https://img.shields.io/badge/Network-Netty-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

> **"레거시 C# 엔진을 Java 17 및 Netty 기반의 고성능 비동기 게이트웨이로 재설계한 마이그레이션 프로젝트입니다."**

본 프로젝트는 기존 C# 기반 IoT 게이트웨이를 Java/Spring Boot 환경으로 이전하며 **고성능 바이너리 데이터 처리**와 **확장성 있는 백엔드 구조**를 확보하는 데 집중했습니다.

---

## 🛠 Tech Stack
* **Language**: Java 17 (Eclipse Temurin)
* **Framework**: Spring Boot 3.3.x
* **Network**: Netty 4.1.x (Event-driven, Non-blocking I/O)
* **Build Tool**: Maven (with Maven Wrapper)
* **Infrastructure**: Docker (Multi-stage Build)

---

## 🔥 핵심 문제 해결 (Troubleshooting Case Study)

### 1. JDK 17 마이그레이션과 `tools.jar` 라이브러리 부재 해결
* **문제**: JDK 17로 전환하며 더 이상 존재하지 않는 `tools.jar` 경로를 빌드 도구가 참조하여 컴파일 에러 발생.
* **원인**: 과거 자바 버전의 환경 변수 및 IDE 설정이 빌드 엔진과 충돌함.
* **해결**: 
  * **Maven Wrapper**를 도입하여 프로젝트별 독립적인 빌드 환경을 구축.
  * IntelliJ의 **Maven Importer/Runner JRE** 설정을 프로젝트 SDK(17)로 강제 동기화하여 환경 의존성 문제 해결.

### 2. 고성능 바이너리 패킷 파싱
* **문제**: 수천 개의 장치에서 들어오는 실시간 바이너리 패킷의 효율적 처리 필요.
* **해결**: Netty의 **Event-driven** 아키텍처를 도입하여 논블로킹 방식으로 패킷을 수신하고, 메모리 최적화 파싱 로직 구현.

---

## 🏗 System Architecture



* **Port 8003 (Netty)**: IoT 장치로부터 바이너리 데이터를 수신하는 고속 통로.
* **Port 8080 (Tomcat)**: 서버 상태 모니터링 및 REST API 제공.
* **ConfigManager**: 외부 CSV 파일(`device-inventory.csv`)을 통한 유연한 장치 설정 관리.

---

## 🚀 Quick Start (with Docker)

프로젝트를 별도의 설정 없이 Docker 환경에서 즉시 실행할 수 있습니다.

```bash
# 1. 저장소 복제
git clone [https://github.com/](https://github.com/)[사용자ID]/MassFlux-Gateway.git

# 2. 도커 이미지 빌드
docker build -t massflux-gateway .

# 3. 컨테이너 실행
docker run -p 8003:8003 -p 8080:8080 massflux-gateway
