package com.kreconomy.monitor.dto.economy;

public record EcosSeriesSpec(
        String statCode,
        String period,
        String itemCode,
        String seriesName
) {
}
