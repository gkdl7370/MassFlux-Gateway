# 🚀 MassFlux-Gateway (C# to Java Migration)

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-green?logo=springboot)
![Netty](https://img.shields.io/badge/Network-Netty-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

> **"레거시 C# 엔진을 Java 17 및 Netty 기반의 고성능 비동기 게이트웨이로 재설계한 마이그레이션 프로젝트입니다."**

본 프로젝트는 기존 C# 기반 IoT 게이트웨이를 Java/Spring Boot 환경으로 이전하며 **고성능 바이너리 데이터 처리**와 **확장성 있는 백엔드 구조**를 확보하는 데 집중했습니다.

---

## 📡 System Architecture

이 게이트웨이는 Spring Boot의 관리 기능과 Netty의 고성능 네트워크 기능을 하나의 애플리케이션으로 통합한 하이브리드 구조를 가집니다.

```mermaid
graph TD
    subgraph "External World"
        IoT[📡 IoT Devices]
        Admin[👨‍💻 Admin / Monitoring]
    end

    subgraph "MassFlux-Gateway Container (Java 17)"
        direction TB
        Netty[⚡ Netty Server<br>(TCP Port 8003)]
        Tomcat[🍃 Spring Web MVC<br>(HTTP Port 8080)]
        
        subgraph "Core Engine"
            Decoder[⚙️ Binary Decoder<br>(ByteBuf / Little Endian)]
            Handler[🧠 Business Logic Handler]
            Config[📂 Config Manager<br>(CSV Loader)]
        end

        Netty -->|Raw Binary Stream| Decoder
        Decoder -->|Parsed Data Object| Handler
        Tomcat -->|API Request| Handler
        Config -->|Inject Settings| Handler
    end

    IoT -->|TCP Connection| Netty
    Admin -->|HTTP GET/POST| Tomcat

    style Netty fill:#e1f5fe,stroke:#0288d1,stroke-width:2px,color:#000
    style Tomcat fill:#e8f5e9,stroke:#388e3c,stroke-width:2px,color:#000
    style Handler fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,color:#000

Netty Server (Port 8003): 수천 개의 IoT 장비로부터 쏟아지는 바이너리 패킷을 논블로킹(Non-blocking) 방식으로 수신하는 전용 입구입니다.

Spring Boot Web (Port 8080): 게이트웨이의 상태를 모니터링하거나 설정을 변경하기 위한 관리자용 REST API 엔드포인트입니다.


Data Flow Sequence
IoT 장비에서 데이터가 들어왔을 때 내부적으로 어떻게 처리되는지를 보여주는 순서도입니다. C#과의 엔디안 호환성을 위해 Little Endian 파싱을 적용했습니다.

sequenceDiagram
    autonumber
    participant Device as 📡 IoT Device
    participant Netty as ⚡ Netty Engine (8003)
    participant Decoder as ⚙️ ByteToMessageDecoder
    participant Handler as 🧠 Business Handler

    Note over Device, Netty: TCP 연결 수립 (Connection Established)
    Device->>Netty: 바이너리 패킷 전송 (Send Binary Packet)
    activate Netty
    Netty->>Decoder: ByteBuf 데이터 전달
    activate Decoder
    Note right of Decoder: Little Endian 기반<br/>필드 파싱 (Zero-copy 지향)
    Decoder->>Handler: 파싱된 객체(Parsed Object) 전달
    deactivate Decoder
    activate Handler
    Handler->>Handler: 데이터 유효성 검증 및 비즈니스 로직 수행
    Handler-->>Netty: 처리 완료 신호
    deactivate Handler
    Netty-->>Device: ACK (응답 전송)
    deactivate Netty


Category,Technology,Description
Language,Java 17,"Record, Switch Expression 등 최신 문법 활용"
Framework,Spring Boot 3.3.x,애플리케이션 컨텍스트 관리 및 웹 서버 기능 제공
Network,Netty 4.1.x,"핵심 엔진. Event-driven, 비동기 소켓 통신 구현"
Build Tool,Maven,Maven Wrapper를 통한 환경 독립적 빌드 구성
DevOps,Docker,Multi-stage build를 적용한 경량 컨테이너 배포

# 1. 저장소 복제
git clone [https://github.com/](https://github.com/)[본인아이디]/MassFlux-Gateway.git
cd MassFlux-Gateway

# 2. 도커 이미지 빌드 (약 1~2분 소요)
docker build -t massflux-gateway .

# 3. 컨테이너 실행 (8003: 데이터 수신, 8080: 웹 관리)
docker run -p 8003:8003 -p 8080:8080 massflux-gateway
