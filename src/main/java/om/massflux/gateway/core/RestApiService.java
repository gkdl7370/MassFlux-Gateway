package om.massflux.gateway.core;

import om.massflux.gateway.model.SensorPacket;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestApiService {
    private final WebClient webClient;

    public RestApiService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://your-api-server.com") // 실제 API 서버 주소로 수정
                .build();
    }

    public void sendTelemetry(SensorPacket packet) {
        // 데이터를 비동기적으로 전송 (전송 결과와 상관없이 엔진은 다음 데이터를 계속 받음)
        webClient.post()
                .uri("/api/telemetry")
                .body(Mono.just(packet), SensorPacket.class)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> {}, // 성공 시 처리
                        error -> System.err.println("[API Error] " + error.getMessage()) // 실패 시 처리
                );
    }
}