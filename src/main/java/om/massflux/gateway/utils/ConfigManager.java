package om.massflux.gateway.utils;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigManager {
    // 장비 ID(Key)를 주면 사이트 코드(Value)를 반환하는 맵
    // 검색 시 O(1)
    private final Map<String, String> deviceInventory = new HashMap<>();

    @PostConstruct
    public void init() {
        // 프로그램 시작 시 CSV 파일을 읽어 메모리에 올림
        loadDeviceInventory("device-inventory.csv");
    }

    private void loadDeviceInventory(String fileName) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    deviceInventory.put(parts[0].trim(), parts[1].trim());
                }
            }
            System.out.println("[Config] Loaded " + deviceInventory.size() + " devices from inventory.");
        } catch (Exception e) {
            System.err.println("[Config Error] 장비 목록을 불러오지 못했습니다: " + e.getMessage());
        }
    }

    // Netty의 여러 Worker 스레드들이 동시에 이 메서드 호출
    // 읽기 전용으로 접근하므로 Thread-Safe하며 락 경합이 발생 방지
    public String getSiteCode(String deviceId) {
        // 장비가 목록에 없을 경우 시스템이 멈추지 않도록 기본값("UNKNOWN")을 반환 - 무중단을 위한 방어적 로직
        return deviceInventory.getOrDefault(deviceId, "UNKNOWN");
    }
}