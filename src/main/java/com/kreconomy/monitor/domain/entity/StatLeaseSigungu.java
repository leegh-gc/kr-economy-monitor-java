package com.kreconomy.monitor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "stat_lease_sigungu")
@Getter
@NoArgsConstructor
public class StatLeaseSigungu {

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

    @Column(name = "rent_gbn")
    private String rentGbn;

    @Column(name = "min_deposit")
    private BigDecimal minDeposit;

    @Column(name = "avg_deposit")
    private BigDecimal avgDeposit;

    @Column(name = "max_deposit")
    private BigDecimal maxDeposit;

    @Column(name = "total_count")
    private Integer totalCount;
}
