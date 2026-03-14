package com.kreconomy.monitor.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ecos_data")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EcosData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "stat_code", nullable = false, length = 8)
    private String statCode;

    @Column(name = "stat_name", nullable = false, length = 200)
    private String statName;

    @Column(name = "item_code1", length = 20)
    private String itemCode1;

    @Column(name = "item_name1", length = 200)
    private String itemName1;

    @Column(name = "item_code2", length = 20)
    private String itemCode2;

    @Column(name = "item_name2", length = 200)
    private String itemName2;

    @Column(name = "item_code3", length = 20)
    private String itemCode3;

    @Column(name = "item_name3", length = 200)
    private String itemName3;

    @Column(name = "item_code4", length = 20)
    private String itemCode4;

    @Column(name = "item_name4", length = 200)
    private String itemName4;

    @Column(name = "year", nullable = false, length = 4)
    private String year;

    @Column(name = "date", nullable = false, length = 10)
    private String date;

    @Column(name = "data_value")
    private Double dataValue;

    @Column(name = "unit_name", length = 50)
    private String unitName;

    // DB 기본값 now()로 자동 설정 — 인서트/업데이트 시 제외
    @Column(name = "createAt", insertable = false, updatable = false)
    private OffsetDateTime createAt;
}
