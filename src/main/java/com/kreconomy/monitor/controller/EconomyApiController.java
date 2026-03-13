package com.kreconomy.monitor.controller;

import com.kreconomy.monitor.dto.economy.*;
import com.kreconomy.monitor.service.CacheService;
import com.kreconomy.monitor.service.EcosService;
import com.kreconomy.monitor.service.GeminiService;
import com.kreconomy.monitor.service.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/economy")
@RequiredArgsConstructor
public class EconomyApiController {

    private final EcosService ecosService;
    private final GeminiService geminiService;
    private final CacheService cacheService;
    private final PromptBuilder promptBuilder;

    private static final String ECONOMY_CACHE_KEY = "economy_analysis";
    private static final String ECONOMY_CARTOON_KEY = "economy_cartoon";

    @GetMapping("/interest-rate")
    public EconomySectionResponse getInterestRate() {
        return ecosService.fetchSection("interest-rate", List.of(
                new EcosSeriesSpec("722Y001", "M", "0101000", "기준금리"),
                new EcosSeriesSpec("817Y002", "D", "010200000", "국고채 3년"),
                new EcosSeriesSpec("817Y002", "D", "010210000", "국고채 10년")
        ));
    }

    @GetMapping("/gdp")
    public EconomySectionResponse getGdp() {
        return ecosService.fetchSection("gdp", List.of(
                new EcosSeriesSpec("902Y015", "Q", "KOR", "GDP 성장률(전기비)"),
                new EcosSeriesSpec("902Y018", "A", "KOR", "1인당 GDP")
        ));
    }

    @GetMapping("/exchange-rate")
    public EconomySectionResponse getExchangeRate() {
        return ecosService.fetchSection("exchange-rate", List.of(
                new EcosSeriesSpec("731Y001", "D", "0000001", "원/달러(USD)"),
                new EcosSeriesSpec("731Y001", "D", "0000003", "원/유로(EUR)"),
                new EcosSeriesSpec("731Y001", "D", "0000002", "원/엔(JPY 100엔)"),
                new EcosSeriesSpec("731Y001", "D", "0000053", "원/위안(CNY)")
        ));
    }

    @GetMapping("/price-index")
    public EconomySectionResponse getPriceIndex() {
        return ecosService.fetchSection("price-index", List.of(
                new EcosSeriesSpec("901Y009", "M", "0", "소비자물가지수(CPI)"),
                new EcosSeriesSpec("404Y014", "M", "*AA", "생산자물가지수(PPI)")
        ));
    }

    @GetMapping("/trade")
    public EconomySectionResponse getTrade() {
        return ecosService.fetchSection("trade", List.of(
                new EcosSeriesSpec("301Y013", "M", "000000", "경상수지"),
                new EcosSeriesSpec("901Y118", "M", "T002", "수출금액"),
                new EcosSeriesSpec("901Y118", "M", "T004", "수입금액")
        ));
    }

    @GetMapping("/employment")
    public EconomySectionResponse getEmployment() {
        return ecosService.fetchSection("employment", List.of(
                new EcosSeriesSpec("901Y027", "M", "I61BC", "실업률"),
                new EcosSeriesSpec("901Y027", "M", "I61BA", "취업자수")
        ));
    }

    @GetMapping("/money-supply")
    public EconomySectionResponse getMoneySupply() {
        return ecosService.fetchSection("money-supply", List.of(
                new EcosSeriesSpec("902Y005", "M", "KR", "M2 광의통화"),
                new EcosSeriesSpec("902Y014", "M", "KR", "외환보유액(한국)"),
                new EcosSeriesSpec("902Y014", "M", "JP", "외환보유액(일본)"),
                new EcosSeriesSpec("902Y014", "M", "CN", "외환보유액(중국)")
        ));
    }

    @GetMapping("/population")
    public EconomySectionResponse getPopulation() {
        return ecosService.fetchSection("population", List.of(
                new EcosSeriesSpec("901Y028", "A", "I35A", "추계인구(전체)"),
                new EcosSeriesSpec("901Y028", "A", "I35B", "추계인구(남)"),
                new EcosSeriesSpec("901Y028", "A", "I35C", "추계인구(여)"),
                new EcosSeriesSpec("901Y028", "A", "I35D", "고령인구비율"),
                new EcosSeriesSpec("901Y028", "A", "I35E", "합계출산율")
        ));
    }

    @GetMapping("/analysis")
    public ResponseEntity<AnalysisResponse> getAnalysis() {
        Map<String, Object> allData = fetchAllEconomyData();
        String currentHash = cacheService.computeHash(allData);

        if (!cacheService.isStale(ECONOMY_CACHE_KEY, currentHash)) {
            String analysis = cacheService.getAnalysis(ECONOMY_CACHE_KEY);
            String createdAt = cacheService.getCreatedAt(ECONOMY_CACHE_KEY);
            return ResponseEntity.ok(new AnalysisResponse(analysis, createdAt, true));
        }

        String prompt = promptBuilder.buildEconomyPrompt(allData);
        String analysis = geminiService.generateAnalysis(prompt);
        cacheService.setCache(ECONOMY_CACHE_KEY, currentHash, analysis, null);

        return ResponseEntity.ok(new AnalysisResponse(analysis, null, false));
    }

    @GetMapping("/cartoon")
    public ResponseEntity<CartoonResponse> getCartoon() {
        String analysisText = cacheService.getAnalysis(ECONOMY_CACHE_KEY);
        if (analysisText == null) analysisText = "";

        String cachedCartoon = cacheService.getCartoonB64(ECONOMY_CARTOON_KEY);
        if (cachedCartoon != null) {
            String createdAt = cacheService.getCreatedAt(ECONOMY_CARTOON_KEY);
            return ResponseEntity.ok(new CartoonResponse(cachedCartoon, createdAt));
        }

        String cartoonB64 = geminiService.generateCartoon(analysisText, "경제");
        String hash = cacheService.computeHash(Map.of("analysis", analysisText));
        cacheService.setCache(ECONOMY_CARTOON_KEY, hash, analysisText, cartoonB64);

        return ResponseEntity.ok(new CartoonResponse(cartoonB64, null));
    }

    private Map<String, Object> fetchAllEconomyData() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Object[]> sectionSpecs = List.of(
                new Object[]{"interest-rate", List.of(
                        new EcosSeriesSpec("722Y001", "M", "0101000", "기준금리"),
                        new EcosSeriesSpec("817Y002", "D", "010200000", "국고채 3년"),
                        new EcosSeriesSpec("817Y002", "D", "010210000", "국고채 10년")
                )},
                new Object[]{"gdp", List.of(
                        new EcosSeriesSpec("902Y015", "Q", "KOR", "GDP 성장률(전기비)"),
                        new EcosSeriesSpec("902Y018", "A", "KOR", "1인당 GDP")
                )},
                new Object[]{"exchange-rate", List.of(
                        new EcosSeriesSpec("731Y001", "D", "0000001", "원/달러(USD)"),
                        new EcosSeriesSpec("731Y001", "D", "0000053", "원/위안(CNY)")
                )},
                new Object[]{"price-index", List.of(
                        new EcosSeriesSpec("901Y009", "M", "0", "소비자물가지수(CPI)"),
                        new EcosSeriesSpec("404Y014", "M", "*AA", "생산자물가지수(PPI)")
                )},
                new Object[]{"trade", List.of(
                        new EcosSeriesSpec("301Y013", "M", "000000", "경상수지"),
                        new EcosSeriesSpec("901Y118", "M", "T002", "수출금액"),
                        new EcosSeriesSpec("901Y118", "M", "T004", "수입금액")
                )},
                new Object[]{"employment", List.of(
                        new EcosSeriesSpec("901Y027", "M", "I61BC", "실업률"),
                        new EcosSeriesSpec("901Y027", "M", "I61BA", "취업자수")
                )},
                new Object[]{"money-supply", List.of(
                        new EcosSeriesSpec("902Y005", "M", "KR", "M2 광의통화"),
                        new EcosSeriesSpec("902Y014", "M", "KR", "외환보유액(한국)")
                )},
                new Object[]{"population", List.of(
                        new EcosSeriesSpec("901Y028", "A", "I35A", "추계인구(전체)"),
                        new EcosSeriesSpec("901Y028", "A", "I35E", "합계출산율")
                )}
        );

        for (Object[] entry : sectionSpecs) {
            String section = (String) entry[0];
            @SuppressWarnings("unchecked")
            List<EcosSeriesSpec> specs = (List<EcosSeriesSpec>) entry[1];
            try {
                data.put(section, ecosService.fetchSection(section, specs));
            } catch (Exception e) {
                log.warn("Failed to fetch section {} for analysis: {}", section, e.getMessage());
            }
        }
        return data;
    }
}
