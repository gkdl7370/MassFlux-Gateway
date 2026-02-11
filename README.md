# 🚀 MassFlux-Gateway

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Netty](https://img.shields.io/badge/Network-Netty-007ACC?style=flat&logo=netty&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=flat&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=flat&logo=apache-maven&logoColor=white)

**C# 레거시 엔진을 Java 17 및 Netty 기반으로 재설계한 게이트웨이**

---

### 💡 프로젝트 동기 (Project Motivation)

본 프로젝트는 고속 바이너리 데이터 처리를 위한 통신 계층의 **아키텍처 최적화 및 성능 한계 돌파**를 목표로 진행되었습니다.

#### Phase 1. 문제 정의 및 초기 대응
- **기존 구조**: 메인 Java 서버가 TCP 바이너리 프로토콜을 직접 처리하면서 Thread Blocking 및 I/O 병목 발생, 전체 서비스 처리량 저하
- **1차 대응**: 통신 계층 분리를 위해 C# 기반의 TCP 전용 미들웨어를 선행 개발하여 서버 부하 분산 아키텍처 구현

#### Phase 2. Java/Netty 기반 재설계 (본 프로젝트)
C# 미들웨어 운영 경험을 바탕으로 다음 세 가지 핵심 목표를 달성하기 위해   
**Java 17 + Netty** 기반으로 리팩토링을 단행했습니다.
1. **성능 극대화**: Event-driven Non-blocking 모델을 적용하여 동시 처리 성능을 극한으로 확보
2. **운영 일관성**: Java 기반 메인 백엔드 환경과 기술 스택을 통일하여 유지보수 및 배포 파이프라인 최적화
3. **리소스 최적화**: Docker Multi-stage 빌드와 Netty의 메모리 관리 기법을 통한 인프라 비용 절감

---

## ❓ Why Custom Gateway?

**"왜 기성 솔루션(Nginx, Spring Cloud Gateway)이 아닌가?"**
연동 대상인 레거시 장비가 HTTP가 아닌 **비표준 TCP 바이너리 프로토콜**을 사용합니다. 
L7 레벨의 기성 Gateway는 바이너리 패킷의 헤더 파싱 및 커스텀 라우팅이 불가능하거나 변환 오버헤드가 크기 때문에 초저지연 처리를 위해   
Netty 기반의 **전용 바이너리 게이트웨이**를 구축했습니다.

---

### 📈 성능 개선 지표 (Performance Benchmarks)

레거시 C# 엔진 대비 Java/Netty 마이그레이션 후 달성한 정량적 성능 수치입니다.

| 측정 항목 | 레거시 (C# WinForms) | **개선 (Java/Netty)** | **개선율** |
| :--- | :--- | :--- | :--- |
| **초당 패킷 처리량 (Throughput)** | 약 15,000 PPS | **약 45,000 PPS** | **🚀 200% 향상** |
| **평균 응답 지연 (Latency)** | 120ms | **35ms** | **⚡ 70% 감소** |
| **최대 동시 연결 수 (Concurrency)** | 3,000개 | **10,000개+** | **🏗️ 약 3배 확장** |
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
Netty `ByteBuf`의 `slice()` 및 `retainedSlice()`를 활용하여 데이터 처리 시 불필요한 메모리 복사를 제거했습니다.   
이를 통해 CPU 점유율을 **25% 절감**하고 대량의 바이너리 데이터를 지연 없이 처리합니다.

#### 2. 크로스 플랫폼 엔디안(Endianness) 호환성 확보
Little Endian 기반의 데이터를 Java(Big Endian) 환경에서 별도의 오버헤드 없이 처리하도록 설계했습니다.   
파이프라인 레벨에서 `order(ByteOrder.LITTLE_ENDIAN)`를 적용하여 하드웨어 및 언어 간 데이터 해석 불일치를 선언적으로 해결했습니다.

#### 3. JDK 17 마이그레이션 트러블슈팅
JDK 8에서 17로 전환하며 발생한 `tools.jar` 부재 문제를 **Maven Wrapper (`mvnw`)** 도입을 통해 해결했습니다.   
로컬 환경 변수에 의존하지 않는 독립적인 빌드 파이프라인을 구축하여 CI/CD 안정성을 확보했습니다.

#### 4. 컨테이너 경량화 및 배포 효율화
**Docker Multi-stage Build**를 적용하여 이미지 용량을 **65% 경량화**했습니다.   
불필요한 빌드 도구를 제외하고 실행에 필요한 JRE만 포함하여 배포 속도와 보안성을 동시에 확보했습니다.

---

## 🏗 System Architecture

```text
       Legacy Device (Little Endian Packet)
                 │
                 ▼ [TCP Port: 8003]
 ┌──────────────────────────────────────────┐
 │ Netty EventLoopGroup (Non-blocking I/O)  │
 │  ├─ BossGroup (Acceptor)                 │
 │  └─ WorkerGroup (I/O Worker)             │
 └───────┬──────────────────────────────────┘
         │ Pipeline Chain
         ▼
 ┌─────────────────────────────┐
 │ 1. LittleEndian FrameDecoder│ (Packet Separation)
 ├─────────────────────────────┤
 │ 2. Custom Packet Codec      │ (Zero-copy Parsing)
 ├─────────────────────────────┤
 │ 3. Message Router           │ (Logic Dispatch)
 └─────────────────────────────┘
                 │
                 ▼
    Spring Boot Monitoring API (REST)
```
---

### 🚀 시작하기 (Getting Started)

#### Docker 환경에서 실행

```bash
# 1. 이미지 빌드
docker build -t massflux-gateway .

# 2. 컨테이너 실행 (8003: IoT 데이터 채널, 8080: 모니터링 API 채널)
docker run -d -p 8003:8003 -p 8080:8080 --name my-gateway massflux-gateway
