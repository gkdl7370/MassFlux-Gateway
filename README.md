# MassFlux-Gateway

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat&logo=openjdk&logoColor=white)]()
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)]()
[![Netty](https://img.shields.io/badge/Netty-007ACC?style=flat&logo=netty&logoColor=white)]()
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)]()
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat&logo=kubernetes&logoColor=white)]()
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat&logo=github-actions&logoColor=white)]()

**C# 레거시 엔진을 Java 17 + Netty 기반으로 재설계한 범용 고성능 비동기 통신 게이트웨이**

---

## 설계 동기

C# 미들웨어 운영 경험에서 두 가지 한계를 인식했습니다.

1. **OS 종속**: Windows 전용 구조로 Linux 클라우드 이관 불가
2. **동시 연결 한계**: Thread-per-Connection 방식으로 연결 수 증가 시 Context Switching 폭증

이를 해결하기 위해 Java 17 + Netty 기반의 **언어·OS 종속 없는 범용 통신 엔진**으로 재설계했습니다.

---

## 성능 개선 지표

| 측정 항목 | 레거시 (C# WinForms) | 개선 (Java/Netty) | 개선율 |
|-----------|---------------------|-------------------|--------|
| 초당 패킷 처리량 | 약 15,000 PPS | **약 45,000 PPS** | 200% 향상 |
| 평균 응답 지연 | 120ms | **35ms** | 70% 감소 |
| 최대 동시 연결 수 | 3,000개 | **10,000개+** | 약 3배 확장 |
| Docker 이미지 용량 | 약 600MB | **210MB** | 65% 경량화 |

---

## 핵심 기술 구현

### 1. Netty Event-Loop Non-blocking I/O

Thread-per-Connection 방식은 연결 수만큼 스레드가 생성되어 Context Switching 비용이 선형 증가합니다.  
Event-Loop 모델은 소수의 고정 스레드로 수만 개의 연결을 비동기 처리합니다.

```
bossGroup  (1 thread)  — 연결 수락만 담당
workerGroup (N thread) — 실제 I/O 이벤트 처리
```

### 2. Zero-Copy 기반 CPU 오버헤드 제거

`ByteBuf.slice()` / `retainedSlice()`로 메모리 복사 없이 원본 버퍼의 특정 구간만 참조합니다.  
불필요한 객체 생성을 줄여 GC 압박을 최소화합니다.

### 3. 장애 대응: 재시도 + 예외 처리

- API 전송 실패 시 최대 3회, 2초 간격 자동 재시도
- 채널 오류 발생 시 `exceptionCaught`로 안전하게 연결 종료
- ByteBuf 참조 카운트 명시적 해제 (메모리 누수 방지)

### 4. Docker Multi-stage Build 경량화

빌드 도구를 최종 이미지에서 제외하고 JRE만 포함합니다.  
보안 강화 + 이미지 용량 65% 절감 + Linux 클라우드 이관 완료.

```dockerfile
# 1단계: 빌드 환경 (Maven + JDK)
FROM maven:3.9.4-eclipse-temurin-17 AS build
RUN mvn clean package -DskipTests

# 2단계: 실행 환경 (JRE만 포함 → 경량화)
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /app/target/*.jar app.jar
```

### 5. Kubernetes 배포 및 HPA 오토스케일링

Deployment + Service + HPA 구성으로 고가용성과 자동 스케일링을 적용했습니다.

**구성 요소:**
- `Deployment`: replicas 2로 파드 이중화, Rolling Update로 무중단 배포
- `Service`: NodePort 타입으로 외부 접근 경로 제공 (8003, 8080)
- `HPA`: CPU 30% 임계치 기준 파드 자동 증설 (min 2 / max 5)

**부하 테스트 결과:**

| 상태 | CPU 사용률 | 파드 수 |
|------|-----------|---------|
| 평상시 | 0% | 2개 |
| 부하 발생 | 84% | 5개 (자동 증설) |
| 부하 제거 후 | 0% | 2개 (자동 축소) |

트래픽 증가 시 인프라 개입 없이 처리 용량이 자동으로 확장되었고,  
스케일 다운 안정화 윈도우(5분) 경과 후 정상 축소를 확인했습니다.

### 6. GitHub Actions CI/CD 파이프라인

main 브랜치 push 시 빌드 → 이미지 생성 → 배포까지 자동화했습니다.

```
코드 push → GitHub Actions 트리거
  → Maven 빌드 + 테스트
  → Docker 이미지 빌드
  → 배포
```

---

## 시스템 아키텍처

### 애플리케이션 구조

```
Legacy Device (Little Endian Packet)
        │
        ▼ [TCP Port: 8003]
┌─────────────────────────────────────┐
│ Netty EventLoopGroup (Non-blocking) │
│  ├─ BossGroup  (연결 수락)           │
│  └─ WorkerGroup (I/O 처리)          │
└───────────┬─────────────────────────┘
            │ Pipeline
            ▼
┌─────────────────────────┐
│ 1. FluxDecoder          │ ← Zero-copy 패킷 파싱
├─────────────────────────┤
│ 2. GatewayHandler       │ ← 비즈니스 로직 처리
├─────────────────────────┤
│ 3. RestApiService       │ ← 비동기 API 전송 + 재시도
└─────────────────────────┘
```

### Kubernetes 배포 구조

```
외부 트래픽
     │
     ▼
┌─────────────────────────────────┐
│ Service (NodePort)              │
│  ├─ 8003: Netty TCP             │
│  └─ 8080: REST API / Actuator   │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ Deployment                      │
│  ├─ Pod 1 (massflux-gateway)    │
│  └─ Pod 2 (massflux-gateway)    │ ← replicas: 2 (기본)
└──────────────┬──────────────────┘
               │ CPU 모니터링
               ▼
┌─────────────────────────────────┐
│ HPA (Horizontal Pod Autoscaler) │
│  ├─ 임계치: CPU 30%             │
│  ├─ min: 2개 / max: 5개         │
│  └─ 부하 시 자동 증설            │
└─────────────────────────────────┘

CI/CD: GitHub Actions
  main push → 빌드 → 이미지 → 배포 자동화
```

---

## Kubernetes로 실행

```bash
# 1. minikube 클러스터 시작
minikube start --driver=docker

# 2. 이미지 빌드 및 로드
docker build -t massflux-gateway:latest .
minikube image load massflux-gateway:latest

# 3. 배포
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml

# 4. 상태 확인
kubectl get pods
kubectl get hpa

# 5. 접속 URL 확인
minikube service massflux-gateway-svc --url
```

### HPA 동작 확인 (부하 테스트)

```bash
# 터미널 1: HPA 모니터링
kubectl get hpa -w

# 터미널 2: 부하 발생
kubectl run load-generator --image=busybox --restart=Never \
  -- /bin/sh -c "while true; do wget -q -O- http://massflux-gateway-svc:8080/actuator/health; done"

# 부하 제거
kubectl delete pod load-generator
```

---

## Docker로 실행

```bash
# Docker 빌드
docker build -t massflux-gateway .

# 실행 (8003: 데이터 수신, 8080: Spring Boot)
docker run -d \
  -p 8003:8003 \
  -p 8080:8080 \
  -e API_SERVER_URL=http://api-server.com \
  --name massflux-gateway \
  massflux-gateway
```

---

## 설정

`application.properties`에서 관리합니다.

```properties
gateway.port=8003
api.server.url=http://api-server.com
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```
