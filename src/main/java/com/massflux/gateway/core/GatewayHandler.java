package com.massflux.gateway.core;

import com.massflux.gateway.model.SensorPacket;
import com.massflux.gateway.utils.ConfigManager;
import com.massflux.gateway.utils.FluxDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class GatewayHandler extends ChannelInboundHandlerAdapter {

    private final ConfigManager configManager;
    private final RestApiService apiService;

    public GatewayHandler(ConfigManager configManager, RestApiService apiService) {
        this.configManager = configManager;
        this.apiService = apiService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            // 최소 패킷 길이(40byte) + STX(0x02) 헤더 검증
            // TODO: 체크섬 검증 추가 예정
            if (in.readableBytes() < 40 || in.getByte(0) != 0x02) {
                log.warn("[Gateway] 유효하지 않은 패킷 수신 - 무시");
                return;
            }

            String deviceId = FluxDecoder.readAscii(in, 8, 10);
            String siteCode = configManager.getSiteCode(deviceId);

            SensorPacket packet = SensorPacket.builder()
                    .deviceId(deviceId)
                    .siteCode(siteCode)
                    .valueX(FluxDecoder.readFloatLE(in, 24))
                    .valueY(FluxDecoder.readFloatLE(in, 28))
                    .valueZ(FluxDecoder.readFloatLE(in, 32))
                    .timestamp(LocalDateTime.now().toString())
                    // battery는 현재 프로토콜 미포함 (기본값 0)
                    .build();

            apiService.sendTelemetry(packet);

        } finally {
            // ByteBuf 참조 카운트 해제 — 누락 시 메모리 누수
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[Gateway] 채널 오류 - 연결 종료: {}", cause.getMessage());
        ctx.close();
    }
}