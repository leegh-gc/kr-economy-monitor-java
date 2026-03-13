package com.kreconomy.monitor.dto.realestate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DistrictStatsResponse {
    private String sigunguCode;
    private List<PriceStatsDto> sale;
    private List<LeaseStatsDto> lease;
}
