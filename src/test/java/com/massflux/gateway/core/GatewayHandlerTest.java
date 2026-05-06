package com.massflux.gateway.core;

import com.massflux.gateway.model.SensorPacket;
import com.massflux.gateway.utils.ConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GatewayHandlerTest {

    @Test
    @DisplayName("정상 패킷을 SensorPacket으로 변환해 API 서비스로 전달한다")
    void channelReadDecodesValidPacketAndSendsTelemetry() {
        ConfigManager configManager = mock(ConfigManager.class);
        RestApiService apiService = mock(RestApiService.class);
        when(configManager.getSiteCode("DEV-000001")).thenReturn("SITE-A");

        ByteBuf packet = validPacket("DEV-000001", 1.5f, 2.5f, 3.5f);
        EmbeddedChannel channel = new EmbeddedChannel(new GatewayHandler(configManager, apiService));

        assertThat(channel.writeInbound(packet)).isFalse();

        verify(apiService).sendTelemetry(argThat((SensorPacket sensor) ->
                sensor.getDeviceId().equals("DEV-000001")
                        && sensor.getSiteCode().equals("SITE-A")
                        && sensor.getValueX() == 1.5f
                        && sensor.getValueY() == 2.5f
                        && sensor.getValueZ() == 3.5f
        ));
    }

    @Test
    @DisplayName("길이가 짧거나 STX가 잘못된 패킷은 무시한다")
    void channelReadDropsMalformedPacket() {
        ConfigManager configManager = mock(ConfigManager.class);
        RestApiService apiService = mock(RestApiService.class);
        ByteBuf packet = Unpooled.wrappedBuffer(new byte[]{0x01, 0x02, 0x03});
        EmbeddedChannel channel = new EmbeddedChannel(new GatewayHandler(configManager, apiService));

        assertThat(channel.writeInbound(packet)).isFalse();

        verifyNoInteractions(apiService);
    }

    private ByteBuf validPacket(String deviceId, float x, float y, float z) {
        ByteBuf packet = Unpooled.buffer(40);
        packet.writeZero(40);
        packet.setByte(0, 0x02);
        packet.setCharSequence(8, deviceId, StandardCharsets.US_ASCII);
        packet.setFloatLE(24, x);
        packet.setFloatLE(28, y);
        packet.setFloatLE(32, z);
        return packet;
    }
}
