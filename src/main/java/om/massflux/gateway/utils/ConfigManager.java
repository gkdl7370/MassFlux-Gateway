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
    private final Map<String, String> deviceInventory = new HashMap<>();

    @PostConstruct
    public void init() {
        // 프로그램 시작 시 CSV 파일을 읽어 메모리에 올립니다.
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

    public String getSiteCode(String deviceId) {
        return deviceInventory.getOrDefault(deviceId, "UNKNOWN");
    }
}