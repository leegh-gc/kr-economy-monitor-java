package com.kreconomy.monitor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "rank_uatype_sigungu_lease")
@Getter
@NoArgsConstructor
public class RankUatypeSigunguLease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "deal_year")
    private String dealYear;

    @Column(name = "sigungu_code")
    private String sigunguCode;

    @Column(name = "sigungu_name")
    private String sigunguName;

    @Column(name = "land_dong")
    private String landDong;

    @Column(name = "apt_name")
    private String aptName;

    @Column(name = "build_year")
    private String buildYear;

    @Column(name = "use_area_type")
    private String useAreaType;

    @Column(name = "rank_type")
    private Integer rankType;

    @Column(name = "rent_gbn")
    private String rentGbn;

    @Column(name = "min_deposit")
    private BigDecimal minDeposit;

    @Column(name = "avg_deposit")
    private BigDecimal avgDeposit;

    @Column(name = "max_deposit")
    private BigDecimal maxDeposit;

    @Column(name = "trade_count")
    private Integer tradeCount;
}
