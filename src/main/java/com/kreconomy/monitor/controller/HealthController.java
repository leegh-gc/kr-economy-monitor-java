package com.kreconomy.monitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.kreconomy.monitor.service.CacheService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final CacheService cacheService;

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "version", "1.0.0");
    }

    @PostMapping("/api/cache/invalidate")
    public Map<String, String> invalidateCache(
            @RequestBody(required = false) Map<String, String> body
    ) {
        String key = (body != null) ? body.get("key") : null;
        cacheService.invalidate(key);
        return Map.of("status", "invalidated", "key", key != null ? key : "all");
    }
}
