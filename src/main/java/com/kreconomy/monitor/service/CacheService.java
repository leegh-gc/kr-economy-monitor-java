package com.kreconomy.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Slf4j
@Service
public class CacheService {

    private final File cacheDir;
    private final ObjectMapper objectMapper;

    public CacheService(
            @Value("${app.cache.dir}") String cacheDirPath,
            ObjectMapper objectMapper
    ) {
        this.cacheDir = new File(cacheDirPath);
        this.objectMapper = objectMapper;
        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
    }

    /**
     * 객체를 JSON 직렬화 후 SHA-256 해시값을 반환한다.
     */
    public String computeHash(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Hash computation failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 캐시가 없거나 해시가 다르면 true (stale)
     */
    public boolean isStale(String key, String currentHash) {
        File file = getCacheFile(key);
        if (!file.exists()) return true;
        try {
            JsonNode node = objectMapper.readTree(file);
            String cachedHash = node.path("hash").asText("");
            return !cachedHash.equals(currentHash);
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 캐시에서 분석 텍스트를 반환한다. 없으면 null.
     */
    public String getAnalysis(String key) {
        File file = getCacheFile(key);
        if (!file.exists()) return null;
        try {
            JsonNode node = objectMapper.readTree(file);
            return node.path("analysis").asText(null);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 캐시에서 컷툰 base64를 반환한다. 없으면 null.
     */
    public String getCartoonB64(String key) {
        File file = getCacheFile(key);
        if (!file.exists()) return null;
        try {
            JsonNode node = objectMapper.readTree(file);
            String val = node.path("cartoon_b64").asText(null);
            return (val == null || val.isBlank()) ? null : val;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 캐시 생성 시각을 반환한다. 없으면 null.
     */
    public String getCreatedAt(String key) {
        File file = getCacheFile(key);
        if (!file.exists()) return null;
        try {
            JsonNode node = objectMapper.readTree(file);
            return node.path("created_at").asText(null);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 캐시에 분석/컷툰 결과를 저장한다.
     */
    public void setCache(String key, String hash, String analysis, String cartoonB64) {
        File file = getCacheFile(key);
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("hash", hash);
            node.put("analysis", analysis);
            node.put("cartoon_b64", cartoonB64 != null ? cartoonB64 : "");
            node.put("created_at", Instant.now().toString());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, node);
        } catch (IOException e) {
            log.error("Cache write failed for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * 캐시를 무효화한다. key가 null이면 모든 캐시 삭제.
     */
    public void invalidate(String key) {
        if (key != null) {
            File file = getCacheFile(key);
            if (file.exists()) file.delete();
        } else {
            File[] files = cacheDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }

    private File getCacheFile(String key) {
        return new File(cacheDir, key + ".json");
    }
}
