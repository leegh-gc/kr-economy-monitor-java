package com.kreconomy.monitor.dto.realestate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Top5Response {
    private String sigunguCode;
    private List<Top5SaleDto> sale;
    private List<Top5LeaseDto> lease;
}
