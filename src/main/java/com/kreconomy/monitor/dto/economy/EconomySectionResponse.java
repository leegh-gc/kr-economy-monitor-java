package com.kreconomy.monitor.dto.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EconomySectionResponse {
    private String section;
    private List<EcosSeries> series;
}
