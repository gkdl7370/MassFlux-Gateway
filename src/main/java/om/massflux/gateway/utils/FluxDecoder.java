package om.massflux.gateway.utils;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class FluxDecoder {

    // Netty의 ByteBuf는 데이터 복사 없이 뷰만 생성하므로 매우 빠름
    public static String readAscii(ByteBuf buffer, int index, int length) {
        // 버퍼의 남은 데이터가 읽으려는 길이보다 짧으면 IndexOutOfBoundsException이 발생
        // 워커 스레드가 죽는 것을 방지하기 위한 사전 검증 로직
        if (buffer.readableBytes() < index + length) return "";

        // 원본 배열(byte[])을 새로 만들지 않고 지정된 인덱스부터 길이만큼만 문자열로 변환
        // GC가 정리해야 할 쓰레기 객체 생성을 극단적으로 줄이기 위함
        return buffer.toString(index, length, StandardCharsets.US_ASCII).trim();
    }

    // Bit Shift 연산을 직접 구현하는 오버헤드 대신
    // Netty가 최적화해둔 다이렉트 API(getFloatLE)를 호출하여
    // CPU 클럭 낭비 없이 4바이트(float) 데이터를 정확히 역순으로 읽음
    public static float readFloatLE(ByteBuf buffer, int index) {
        return buffer.getFloatLE(index);
    }
}