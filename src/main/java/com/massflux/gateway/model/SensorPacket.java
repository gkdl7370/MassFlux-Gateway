package com.massflux.gateway.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorPacket {

    private String deviceId;    // 장비 고유 ID
    private String siteCode;    // ConfigManager에서 매핑된 사이트 코드

    private float valueX;       // 센서 X축 측정값
    private float valueY;       // 센서 Y축 측정값
    private float valueZ;       // 센서 Z축 측정값 (미사용 시 기본값 0.0)

    private int battery;        // 배터리 잔량 (미사용 시 기본값 0)
    private String timestamp;   // 수신 시각 (ISO-8601)
}