package com.kreconomy.monitor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "stat_sigungu_yymm")
@Getter
@NoArgsConstructor
public class StatSigunguYymm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "sigungu_code")
    private String sigunguCode;

    @Column(name = "sigungu_name")
    private String sigunguName;

    @Column(name = "deal_yymm")
    private String dealYymm;

    @Column(name = "deal_year")
    private String dealYear;

    @Column(name = "use_area_type")
    private String useAreaType;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "avg_price")
    private BigDecimal avgPrice;

    @Column(name = "max_price")
    private BigDecimal maxPrice;

    @Column(name = "total_count")
    private Integer totalCount;
}
