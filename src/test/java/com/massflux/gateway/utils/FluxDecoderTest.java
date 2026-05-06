package com.massflux.gateway.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FluxDecoderTest {

    @Test
    @DisplayName("ASCII 구간을 잘라 공백을 제거하고 readerIndex는 유지한다")
    void readAsciiReturnsTrimmedAsciiSliceWithoutMovingReaderIndex() {
        ByteBuf buffer = Unpooled.wrappedBuffer("STX12345DEVICE-001   TAIL".getBytes());

        String deviceId = FluxDecoder.readAscii(buffer, 8, 13);

        assertThat(deviceId).isEqualTo("DEVICE-001");
        assertThat(buffer.readerIndex()).isZero();
    }

    @Test
    @DisplayName("요청한 구간이 버퍼 범위를 넘으면 빈 문자열을 반환한다")
    void readAsciiReturnsEmptyStringWhenSliceIsOutOfBounds() {
        ByteBuf buffer = Unpooled.wrappedBuffer("short".getBytes());

        assertThat(FluxDecoder.readAscii(buffer, 2, 10)).isEmpty();
    }

    @Test
    @DisplayName("little-endian 형식의 float 값을 읽는다")
    void readFloatLEReadsLittleEndianFloat() {
        ByteBuf buffer = Unpooled.buffer(4);
        buffer.writeFloatLE(12.5f);

        assertThat(FluxDecoder.readFloatLE(buffer, 0)).isEqualTo(12.5f);
    }
}
