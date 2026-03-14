package com.kreconomy.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kreconomy.monitor.domain.entity.EcosData;
import com.kreconomy.monitor.domain.repository.EcosDataRepository;
import com.kreconomy.monitor.dto.economy.EcosDataPoint;
import com.kreconomy.monitor.dto.economy.EcosSeries;
import com.kreconomy.monitor.dto.economy.EcosSeriesSpec;
import com.kreconomy.monitor.dto.economy.EconomySectionResponse;
import com.kreconomy.monitor.util.EcosDateRangeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EcosService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EcosDataRepository ecosDataRepository;
    private final String apiKey;
    private final String baseUrl;

    public EcosService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            EcosDataRepository ecosDataRepository,
            @Value("${app.ecos.api-key}") String apiKey,
            @Value("${app.ecos.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ecosDataRepository = ecosDataRepository;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 단일 시리즈 데이터를 조회한다.
     * - D(일별): ECOS API 직접 호출 (DB 캐시 없음)
     * - A/Q/M: DB 우선 조회 → 없으면 API 호출 후 DB 저장
     */
    public List<EcosDataPoint> fetchSeries(EcosSeriesSpec spec) {
        EcosDateRangeUtil.DateRange range = EcosDateRangeUtil.buildRange(spec.period());

        // 일별(D)은 DB 캐시 없이 API 직접 호출
        if ("D".equalsIgnoreCase(spec.period())) {
            return fetchFromApi(spec, range);
        }

        // DB 우선 조회
        List<EcosData> cached = ecosDataRepository
                .findByStatCodeAndItemCode1AndDateGreaterThanEqualAndDateLessThanEqualOrderByDateAsc(
                        spec.statCode(), spec.itemCode(), range.start(), range.end());

        if (!cached.isEmpty()) {
            log.debug("ECOS DB cache hit for {} ({})", spec.seriesName(), spec.statCode());
            return cached.stream()
                    .map(e -> new EcosDataPoint(e.getDate(), e.getDataValue(), spec.seriesName()))
                    .toList();
        }

        // DB에 없으면 API 호출 후 저장
        log.debug("ECOS DB cache miss for {} ({}), fetching from API", spec.seriesName(), spec.statCode());
        List<EcosDataPoint> apiResult = fetchFromApi(spec, range);
        if (!apiResult.isEmpty()) {
            saveToDb(apiResult, spec);
        }
        return apiResult;
    }

    /**
     * ECOS API를 직접 호출하여 데이터를 가져온다.
     */
    private List<EcosDataPoint> fetchFromApi(EcosSeriesSpec spec, EcosDateRangeUtil.DateRange range) {
        String url = String.format(
                "%s/%s/json/kr/1/1000/%s/%s/%s/%s/%s",
                baseUrl, apiKey, spec.statCode(), spec.period(),
                range.start(), range.end(), spec.itemCode()
        );

        try {
            String json = restTemplate.getForObject(url, String.class);
            return parseResponse(json, spec.seriesName());
        } catch (RestClientException e) {
            log.warn("ECOS API call failed for {}: {}", spec.seriesName(), e.getMessage());
            return List.of();
        }
    }

    /**
     * 조회한 API 결과를 ecos_data 테이블에 저장한다.
     */
    private void saveToDb(List<EcosDataPoint> points, EcosSeriesSpec spec) {
        List<EcosData> entities = points.stream()
                .map(p -> EcosData.builder()
                        .statCode(spec.statCode())
                        .statName(spec.seriesName())
                        .itemCode1(spec.itemCode())
                        .itemName1(spec.seriesName())
                        .year(p.getDate().length() >= 4 ? p.getDate().substring(0, 4) : p.getDate())
                        .date(p.getDate())
                        .dataValue(p.getValue())
                        .build())
                .toList();
        try {
            ecosDataRepository.saveAll(entities);
            log.debug("Saved {} rows to ecos_data for {} ({})", entities.size(), spec.seriesName(), spec.statCode());
        } catch (Exception e) {
            log.warn("Failed to save ecos_data for {}: {}", spec.seriesName(), e.getMessage());
        }
    }

    /**
     * 여러 시리즈를 순차적으로 조회하여 EconomySectionResponse로 조립한다.
     */
    public EconomySectionResponse fetchSection(String sectionName, List<EcosSeriesSpec> specs) {
        List<EcosSeries> series = new ArrayList<>();
        for (EcosSeriesSpec spec : specs) {
            List<EcosDataPoint> data = fetchSeries(spec);
            series.add(new EcosSeries(spec.seriesName(), data));
        }
        return new EconomySectionResponse(sectionName, series);
    }

    private List<EcosDataPoint> parseResponse(String json, String seriesName) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // 에러 응답 처리
            if (root.has("RESULT")) {
                String code = root.path("RESULT").path("CODE").asText("UNKNOWN");
                String msg = root.path("RESULT").path("MESSAGE").asText("Unknown error");
                log.warn("ECOS API error for {}: {} - {}", seriesName, code, msg);
                return List.of();
            }

            JsonNode rows = root.path("StatisticSearch").path("row");
            List<EcosDataPoint> result = new ArrayList<>();
            for (JsonNode row : rows) {
                String rawValue = row.path("DATA_VALUE").asText("");
                try {
                    double value = Double.parseDouble(rawValue);
                    result.add(new EcosDataPoint(
                            row.path("TIME").asText(""),
                            value,
                            seriesName
                    ));
                } catch (NumberFormatException ignored) {
                    // 숫자 변환 불가 행 건너뜀
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to parse ECOS response for {}: {}", seriesName, e.getMessage());
            return List.of();
        }
    }
}
