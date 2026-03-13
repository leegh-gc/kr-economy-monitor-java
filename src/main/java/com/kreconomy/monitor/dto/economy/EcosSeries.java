package com.kreconomy.monitor.dto.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EcosSeries {
    private String name;
    private List<EcosDataPoint> data;
}
