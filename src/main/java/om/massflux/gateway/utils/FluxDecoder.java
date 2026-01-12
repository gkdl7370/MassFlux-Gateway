package om.massflux.gateway.utils;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class FluxDecoder {

    /**
     * 특정 위치에서 ASCII 문자열을 읽어옵니다.
     * Netty의 ByteBuf는 데이터 복사 없이 뷰(View)만 생성하므로 매우 빠릅니다.
     */
    public static String readAscii(ByteBuf buffer, int index, int length) {
        if (buffer.readableBytes() < index + length) return "";
        return buffer.toString(index, length, StandardCharsets.US_ASCII).trim();
    }

    /**
     * Little Endian 방식으로 float(4바이트)를 읽습니다.
     * C#의 BitConverter.ToSingle()과 동일한 역할입니다.
     */
    public static float readFloatLE(ByteBuf buffer, int index) {
        return buffer.getFloatLE(index);
    }
}