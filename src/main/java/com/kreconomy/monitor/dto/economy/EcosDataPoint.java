package com.kreconomy.monitor.dto.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EcosDataPoint {
    private String date;
    private double value;
    private String series;
}
