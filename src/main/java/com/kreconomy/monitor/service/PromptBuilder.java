package com.kreconomy.monitor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final ObjectMapper objectMapper;

    /**
     * 경제 지표 8개 섹션 데이터를 한국어 분석 프롬프트로 변환한다.
     */
    public String buildEconomyPrompt(Object data) {
        String dataJson = toJson(data);
        return """
                다음은 한국 경제 최신 지표 데이터입니다. 이 데이터를 바탕으로 현재 한국 경제 상황을 전문가 시각으로 분석해 주세요.

                ## 데이터
                """ + dataJson + """

                ## 분석 요청
                마크다운 형식으로 아래 구조에 맞춰 작성해 주세요:

                1. **주요 지표별 현황** — 각 지표(금리, GDP, 환율, 물가, 무역, 고용, 통화, 인구)의 최근 동향
                2. **주목할 트렌드** — 특히 주의해야 할 변화나 흐름
                3. **종합 의견** — 현재 한국 경제 상황에 대한 전반적 평가 및 향후 전망

                분석은 명확하고 간결하게, 일반 독자도 이해할 수 있도록 작성해 주세요.
                최대 1500 토큰 이내로 작성하세요.
                """;
    }

    /**
     * 부동산 데이터(KB지수 + 구별 통계)를 한국어 분석 프롬프트로 변환한다.
     */
    public String buildRealestatePrompt(Object data) {
        String dataJson = toJson(data);
        return """
                다음은 서울 부동산 최신 시장 데이터입니다. 이 데이터를 바탕으로 현재 서울 부동산 시장 상황을 전문가 시각으로 분석해 주세요.

                ## 데이터
                """ + dataJson + """

                ## 분석 요청
                마크다운 형식으로 아래 구조에 맞춰 작성해 주세요:

                1. **KB 지수 동향** — 매매지수와 전세지수의 최근 흐름 및 변화
                2. **권역별 현황** — 강남권, 강동권, 강서권, 강북권 등 주요 권역의 가격 동향
                3. **주목할 지역** — 특히 가격 변동이 두드러진 구 또는 아파트
                4. **종합 의견** — 현재 서울 부동산 시장에 대한 전반적 평가 및 향후 전망

                분석은 명확하고 간결하게, 일반 독자도 이해할 수 있도록 작성해 주세요.
                최대 1500 토큰 이내로 작성하세요.
                """;
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return data.toString();
        }
    }
}
