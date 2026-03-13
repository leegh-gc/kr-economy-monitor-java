package com.kreconomy.monitor.dto.realestate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Top5LeaseDto {
    private String dealYear;
    private String sigunguName;
    private String landDong;
    private String aptName;
    private String buildYear;
    private String useAreaType;
    private BigDecimal minDeposit;
    private BigDecimal avgDeposit;
    private BigDecimal maxDeposit;
    private Integer tradeCount;
}
