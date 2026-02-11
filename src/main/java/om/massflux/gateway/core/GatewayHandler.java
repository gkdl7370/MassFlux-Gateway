package om.massflux.gateway.core;

import om.massflux.gateway.model.SensorPacket;
import om.massflux.gateway.utils.ConfigManager;
import om.massflux.gateway.utils.FluxDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatewayHandler extends ChannelInboundHandlerAdapter {
    private final ConfigManager configManager;
    private final RestApiService apiService;

    // 생성자를 통해 스프링 빈들을 전달
    public GatewayHandler(ConfigManager configManager, RestApiService apiService) {
        this.configManager = configManager;
        this.apiService = apiService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            if (in.readableBytes() >= 40 && in.getByte(0) == 0x02) {
                String deviceId = FluxDecoder.readAscii(in, 8, 10);
                String siteCode = configManager.getSiteCode(deviceId); // 사이트 코드 매핑

                SensorPacket packet = SensorPacket.builder()
                        .deviceId(deviceId)
                        .siteCode(siteCode)
                        .valueX(FluxDecoder.readFloatLE(in, 24))
                        .valueY(FluxDecoder.readFloatLE(in, 28))
                        .timestamp(java.time.LocalDateTime.now().toString())
                        .build();

                // 비동기 API 전송 실행
                apiService.sendTelemetry(packet);
            }
        } finally {
            in.release();
        }
    }
}