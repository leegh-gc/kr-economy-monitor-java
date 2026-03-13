package com.kreconomy.monitor.dto.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisResponse {
    private String analysis;
    private String createdAt;
    private boolean cached;
}
