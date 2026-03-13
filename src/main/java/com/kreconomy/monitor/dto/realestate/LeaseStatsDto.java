package com.kreconomy.monitor.dto.realestate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LeaseStatsDto {
    private String sigunguName;
    private String dealYymm;
    private BigDecimal minDeposit;
    private BigDecimal avgDeposit;
    private BigDecimal maxDeposit;
    private Integer totalCount;
}
