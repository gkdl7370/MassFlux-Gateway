sequenceDiagram
    autonumber
    participant Device as "📡 IoT Device"
    participant Netty as "⚡ Netty Engine (8003)"
    participant Decoder as "⚙️ ByteToMessageDecoder"
    participant Handler as "🧠 Business Handler"

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
