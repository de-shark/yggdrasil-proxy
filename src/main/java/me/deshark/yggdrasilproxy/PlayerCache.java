package me.deshark.yggdrasilproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PlayerCache {
    private final Map<String, Integer> playerData = new ConcurrentHashMap<>();
    private final Map<String, Long> keyTimestamp = new ConcurrentHashMap<>();

    // 48 hours in seconds for GC threshold
    private static final long GC_TIME = 172800;

    public void set(String key, Integer value) {
        playerData.put(key, value);
        keyTimestamp.put(key, System.currentTimeMillis() / 1000);
    }

    public Integer get(String key) {
        if (!playerData.containsKey(key)) {
            throw new IllegalArgumentException("Player not found in cache: " + key);
        }
        keyTimestamp.put(key, System.currentTimeMillis() / 1000);
        return playerData.get(key);
    }

    public Long getTimestamp(String key) {
        if (!keyTimestamp.containsKey(key)) {
            throw new IllegalArgumentException("Player timestamp not found: " + key);
        }
        return keyTimestamp.get(key);
    }

    @Scheduled(fixedDelay = 60000) // Run every minute
    public void gcTask() {
        try {
            gc();
        } catch (Exception e) {
            log.error("Error during player cache garbage collection", e);
            log.warn("PlayerCache: {} {}", playerData, keyTimestamp);
        }
    }

    private void gc() {
        long timestamp = System.currentTimeMillis() / 1000;

        for (String key : keyTimestamp.keySet()) {
            if (timestamp - keyTimestamp.get(key) >= GC_TIME) {
                keyTimestamp.remove(key);
                playerData.remove(key);
                log.debug("Removed expired player from cache: {}", key);
            }
        }
    }
}