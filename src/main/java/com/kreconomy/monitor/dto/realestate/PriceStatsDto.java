package com.kreconomy.monitor.dto.realestate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PriceStatsDto {
    private String sigunguName;
    private String dealYymm;
    private BigDecimal minPrice;
    private BigDecimal avgPrice;
    private BigDecimal maxPrice;
    private Integer totalCount;
}
