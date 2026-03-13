package com.kreconomy.monitor.controller;

import com.kreconomy.monitor.dto.economy.AnalysisResponse;
import com.kreconomy.monitor.dto.economy.CartoonResponse;
import com.kreconomy.monitor.dto.economy.EcosSeriesSpec;
import com.kreconomy.monitor.dto.economy.EconomySectionResponse;
import com.kreconomy.monitor.dto.realestate.*;
import com.kreconomy.monitor.service.CacheService;
import com.kreconomy.monitor.service.EcosService;
import com.kreconomy.monitor.service.GeminiService;
import com.kreconomy.monitor.service.PromptBuilder;
import com.kreconomy.monitor.service.RealEstateQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/realestate")
@RequiredArgsConstructor
public class RealEstateApiController {

    private final EcosService ecosService;
    private final RealEstateQueryService realEstateQueryService;
    private final GeminiService geminiService;
    private final CacheService cacheService;
    private final PromptBuilder promptBuilder;

    private static final String RE_ANALYSIS_KEY = "realestate_analysis";
    private static final String RE_CARTOON_KEY = "realestate_cartoon";

    private static final List<String> ZONE_CODES = List.of(
            "11680", "11650", "11710",  // 강남권
            "11740", "11350", "11200",  // 강동권
            "11500", "11560", "11470",  // 강서권
            "11110", "11440", "11170"   // 강북권
    );

    @GetMapping("/kb-index")
    public EconomySectionResponse getKbIndex() {
        return ecosService.fetchSection("kb-index", List.of(
                new EcosSeriesSpec("901Y062", "M", "P63ACA", "매매지수"),
                new EcosSeriesSpec("901Y063", "M", "P64ACA", "전세지수")
        ));
    }

    @GetMapping("/stats/{sigunguCode}")
    public DistrictStatsResponse getStats(
            @PathVariable String sigunguCode,
            @RequestParam(defaultValue = "UA04") String useAreaType
    ) {
        List<PriceStatsDto> sale = realEstateQueryService.fetchSaleStats(sigunguCode, useAreaType);
        List<LeaseStatsDto> lease = realEstateQueryService.fetchLeaseStats(sigunguCode, useAreaType);
        return new DistrictStatsResponse(sigunguCode, sale, lease);
    }

    @GetMapping("/top-apartments/{sigunguCode}")
    public Top5Response getTopApartments(
            @PathVariable String sigunguCode,
            @RequestParam(defaultValue = "UA04") String useAreaType
    ) {
        List<Top5SaleDto> sale = realEstateQueryService.fetchSaleTop5(sigunguCode, useAreaType);
        List<Top5LeaseDto> lease = realEstateQueryService.fetchLeaseTop5(sigunguCode, useAreaType);
        return new Top5Response(sigunguCode, sale, lease);
    }

    @GetMapping("/analysis")
    public ResponseEntity<AnalysisResponse> getAnalysis() {
        Map<String, Object> allData = fetchAllRealestateData();
        String currentHash = cacheService.computeHash(allData);

        if (!cacheService.isStale(RE_ANALYSIS_KEY, currentHash)) {
            String analysis = cacheService.getAnalysis(RE_ANALYSIS_KEY);
            String createdAt = cacheService.getCreatedAt(RE_ANALYSIS_KEY);
            return ResponseEntity.ok(new AnalysisResponse(analysis, createdAt, true));
        }

        String prompt = promptBuilder.buildRealestatePrompt(allData);
        String analysis = geminiService.generateAnalysis(prompt);
        cacheService.setCache(RE_ANALYSIS_KEY, currentHash, analysis, null);

        return ResponseEntity.ok(new AnalysisResponse(analysis, null, false));
    }

    @GetMapping("/cartoon")
    public ResponseEntity<CartoonResponse> getCartoon() {
        String analysisText = cacheService.getAnalysis(RE_ANALYSIS_KEY);
        if (analysisText == null) analysisText = "";

        String cachedCartoon = cacheService.getCartoonB64(RE_CARTOON_KEY);
        if (cachedCartoon != null) {
            String createdAt = cacheService.getCreatedAt(RE_CARTOON_KEY);
            return ResponseEntity.ok(new CartoonResponse(cachedCartoon, createdAt));
        }

        String cartoonB64 = geminiService.generateCartoon(analysisText, "부동산");
        String hash = cacheService.computeHash(Map.of("analysis", analysisText));
        cacheService.setCache(RE_CARTOON_KEY, hash, analysisText, cartoonB64);

        return ResponseEntity.ok(new CartoonResponse(cartoonB64, null));
    }

    private Map<String, Object> fetchAllRealestateData() {
        Map<String, Object> data = new LinkedHashMap<>();

        // KB 지수
        try {
            EconomySectionResponse kbIndex = getKbIndex();
            data.put("kb_index", kbIndex);
        } catch (Exception e) {
            log.warn("Failed to fetch KB index: {}", e.getMessage());
        }

        // 구별 최신 통계 (최근 1개월만)
        Map<String, Object> zoneStats = new LinkedHashMap<>();
        for (String code : ZONE_CODES) {
            try {
                List<PriceStatsDto> sale = realEstateQueryService.fetchSaleStats(code, "UA04");
                List<LeaseStatsDto> lease = realEstateQueryService.fetchLeaseStats(code, "UA04");
                zoneStats.put(code, Map.of(
                        "sale", sale.isEmpty() ? List.of() : List.of(sale.get(0)),
                        "lease", lease.isEmpty() ? List.of() : List.of(lease.get(0))
                ));
            } catch (Exception e) {
                log.warn("Failed to fetch stats for {}: {}", code, e.getMessage());
                zoneStats.put(code, Map.of());
            }
        }
        data.put("zone_stats", zoneStats);

        return data;
    }
}
