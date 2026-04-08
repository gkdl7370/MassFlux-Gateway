package com.massflux.gateway.core;

import com.massflux.gateway.model.SensorPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class RestApiService {

    private final WebClient webClient;

    // application.properties에서 주입 (하드코딩 제거)
    public RestApiService(@Value("${api.server.url}") String apiServerUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(apiServerUrl)
                .build();
    }

    public void sendTelemetry(SensorPacket packet) {
        webClient.post()
                .uri("/api/telemetry")
                .body(Mono.just(packet), SensorPacket.class)
                .retrieve()
                .bodyToMono(String.class)
                // 일시적 장애 시 최대 3회, 2초 간격으로 재시도
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .doBeforeRetry(signal ->
                                log.warn("[API] 재시도 중... 디바이스: {}, 시도: {}회",
                                        packet.getDeviceId(), signal.totalRetries() + 1)))
                .subscribe(
                        response -> log.info("[API] 전송 성공 - 디바이스: {}", packet.getDeviceId()),
                        error -> log.error("[API] 최종 전송 실패 - 디바이스: {}, 원인: {}",
                                packet.getDeviceId(), error.getMessage())
                        // TODO: 재시도 모두 실패 시 로컬 파일 백업 또는 DLQ 연동
                );
    }
}