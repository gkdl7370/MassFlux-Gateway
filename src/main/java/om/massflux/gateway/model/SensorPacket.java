package om.massflux.gateway.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorPacket {
    private String deviceId;
    private String siteCode;
    private float valueX;
    private float valueY;
    private float valueZ;
    private String timestamp;
    private int battery;

    // 필요에 따라 추가 필드 구성 (C# 코드 참고)
}