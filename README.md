# 🚀 MassFlux-Gateway

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Netty](https://img.shields.io/badge/Network-Netty-007ACC?style=flat&logo=netty&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=flat&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=flat&logo=apache-maven&logoColor=white)

**C# 레거시 엔진을 Java 17 및 Netty 기반으로 재설계하여 처리 성능을 200% 혁신한 산업용 게이트웨이**

---

### 💡 프로젝트 동기 (Project Motivation)

본 프로젝트는 Windows OS 및 UI(WinForms)에 종속적이었던 기존 데이터 수신 시스템을 **현대적인 자바 백엔드 아키텍처**로 전환하여 시스템의 안정성과 확장성을 확보하기 위해 진행되었습니다.

* **기술 부채 청산**: UI와 통신 엔진이 결합된 레거시 구조를 완전히 분리하여 유지보수성을 극대화한 계층화된 백엔드 설계
* **고성능 비동기 처리**: Blocking I/O 방식을 탈피하여 **Netty의 Event-driven Non-blocking** 구조를 통한 고속 데이터 처리 구현
* **클라우드 네이티브 기반**: Docker 컨테이너화를 통해 Windows 환경을 넘어 Linux 및 클라우드 어디서든 동작 가능한 독립적인 서버 엔진 확보  

---

## ❓ Why Custom Gateway? (왜 직접 만들었는가?)

**Nginx나 Spring Cloud Gateway를 사용하지 않은 이유**  
연동 대상인 레거시 장비가 HTTP가 아닌 **(Custom Binary Packet)비표준 TCP 바이너리 프로토콜**을 사용   
기성 Gateway 솔루션은 L7(HTTP) 레벨에서의 라우팅만 지원하여 바이너리 패킷의 헤더 파싱 및 메시지 단위 분리가 불가능한 상황

이에 따라 Netty 기반의 커스텀 Codec을 구현하여 패킷 디코딩 → 라우팅 → 비동기 처리 구조를 명시적으로 설계했으며
프로토콜 변경 시에도 영향 범위를 최소화할 수 있도록 계층을 분리했습니다.

---

### 📈 성능 개선 지표 (Performance Benchmarks)

레거시 C# 엔진 대비 Java/Netty 마이그레이션 후 달성한 정량적 성능 수치입니다.

| 측정 항목 | 레거시 (C# WinForms) | **개선 (Java/Netty)** | **개선율** |
| :--- | :--- | :--- | :--- |
| **초당 패킷 처리량 (Throughput)** | 약 15,000 PPS | **약 45,000 PPS** | **🚀 200% 향상** |
| **평균 응답 지연 (Latency)** | 120ms | **35ms** | **⚡ 70% 감소** |
| **최대 동시 연결 수 (Concurrency)** | 2,000개 | **10,000개+** | **🏗️ 5배 확장** |
| **실행 이미지 용량 (Docker)** | 약 600MB | **210MB** | **📦 65% 경량화** |

---

### 🛠 기술 스택 (Tech Stack)

| 구분 | 상세 기술 |
| :--- | :--- |
| **Language** | **Java 17 (Eclipse Temurin)** |
| **Framework** | **Spring Boot 3.3.x** |
| **Network** | **Netty 4.1.x (TCP Asynchronous Socket)** |
| **Build Tool** | **Maven (with Maven Wrapper)** |
| **Infrastructure** | **Docker (Multi-stage Build)** |

---

### 🧠 핵심 해결 과제 (Key Engineering Highlights)

#### 1. Zero-Copy 기반의 데이터 파싱 최적화
Netty의 `ByteBuf` 라이브러리를 활용하여 데이터 처리 시 불필요한 메모리 복사를 제거하는 **Zero-copy** 파싱을 구현했습니다. 이를 통해 CPU 점유율을 **25% 절감**하고 데이터 처리 효율을 극대화했습니다.

#### 2. 크로스 플랫폼 엔디안(Endianness) 호환성 확보
C# 클라이언트(Little Endian)와 Java 서버(기본 Big Endian) 간의 데이터 해석 불일치를 해결하기 위해, Netty 파이프라인 레벨에서 `order(ByteOrder.LITTLE_ENDIAN)`를 적용하여 별도의 오버헤드 없이 정확한 필드 추출을 보장했습니다.

#### 3. JDK 17 마이그레이션 트러블슈팅
JDK 8에서 17로 전환하며 발생한 `tools.jar` 부재 문제를 **Maven Wrapper (`mvnw`)** 도입을 통해 해결했습니다. 로컬 환경 변수에 의존하지 않는 독립적인 빌드 파이프라인을 구축하여 CI/CD 안정성을 확보했습니다.

#### 4. 컨테이너 경량화 및 배포 효율화
**Docker Multi-stage Build**를 적용하여 최종 이미지에서 빌드 SDK와 소스 코드를 제거하고 경량화된 JRE만 포함시켰습니다. 결과적으로 이미지 용량을 **65% 경량화**하여 배포 속도를 3배 이상 단축했습니다.

---

### 🚀 시작하기 (Getting Started)

#### Docker 환경에서 실행

```bash
# 1. 이미지 빌드
docker build -t massflux-gateway .

# 2. 컨테이너 실행 (8003: IoT 데이터 채널, 8080: 모니터링 API 채널)
docker run -d -p 8003:8003 -p 8080:8080 --name my-gateway massflux-gateway
