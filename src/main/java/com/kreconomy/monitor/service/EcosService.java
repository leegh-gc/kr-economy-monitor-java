package com.kreconomy.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final String apiKey;
    private final String baseUrl;

    public EcosService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.ecos.api-key}") String apiKey,
            @Value("${app.ecos.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 단일 시리즈 데이터를 ECOS API에서 조회한다.
     */
    public List<EcosDataPoint> fetchSeries(EcosSeriesSpec spec) {
        EcosDateRangeUtil.DateRange range = EcosDateRangeUtil.buildRange(spec.period());
        String url = String.format(
                "%s/%s/json/kr/1/100/%s/%s/%s/%s/%s",
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
