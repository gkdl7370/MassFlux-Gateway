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
