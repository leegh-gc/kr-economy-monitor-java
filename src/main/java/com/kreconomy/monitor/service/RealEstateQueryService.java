package com.kreconomy.monitor.service;

import com.kreconomy.monitor.domain.entity.*;
import com.kreconomy.monitor.dto.realestate.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealEstateQueryService {

    private final JPAQueryFactory queryFactory;

    /**
     * 매매 월별 통계 조회 (2021~2026, 면적유형 UA04)
     */
    public List<PriceStatsDto> fetchSaleStats(String sigunguCode, String useAreaType) {
        QStatSigunguYymm q = QStatSigunguYymm.statSigunguYymm;

        return queryFactory
                .selectFrom(q)
                .where(
                        q.sigunguCode.eq(sigunguCode),
                        q.useAreaType.eq(useAreaType),
                        q.dealYear.between("2021", "2026")
                )
                .orderBy(q.dealYymm.desc())
                .fetch()
                .stream()
                .map(e -> new PriceStatsDto(
                        e.getSigunguName(),
                        e.getDealYymm(),
                        e.getMinPrice(),
                        e.getAvgPrice(),
                        e.getMaxPrice(),
                        e.getTotalCount()
                ))
                .toList();
    }

    /**
     * 전세 월별 통계 조회 (rent_gbn='0', 2021~2026, 면적유형 UA04)
     */
    public List<LeaseStatsDto> fetchLeaseStats(String sigunguCode, String useAreaType) {
        QStatLeaseSigungu q = QStatLeaseSigungu.statLeaseSigungu;

        return queryFactory
                .selectFrom(q)
                .where(
                        q.sigunguCode.eq(sigunguCode),
                        q.useAreaType.eq(useAreaType),
                        q.rentGbn.eq("0"),
                        q.dealYear.between("2021", "2026")
                )
                .orderBy(q.dealYymm.desc())
                .fetch()
                .stream()
                .map(e -> new LeaseStatsDto(
                        e.getSigunguName(),
                        e.getDealYymm(),
                        e.getMinDeposit(),
                        e.getAvgDeposit(),
                        e.getMaxDeposit(),
                        e.getTotalCount()
                ))
                .toList();
    }

    /**
     * 매매 TOP5 아파트 조회 (2026년, rank_type=0)
     */
    public List<Top5SaleDto> fetchSaleTop5(String sigunguCode, String useAreaType) {
        QRankUatypeSigungu q = QRankUatypeSigungu.rankUatypeSigungu;

        return queryFactory
                .selectFrom(q)
                .where(
                        q.sigunguCode.eq(sigunguCode),
                        q.useAreaType.eq(useAreaType),
                        q.dealYear.eq("2026"),
                        q.rankType.eq(0)
                )
                .orderBy(q.avgPrice.desc())
                .limit(5)
                .fetch()
                .stream()
                .map(e -> new Top5SaleDto(
                        e.getDealYear(),
                        e.getSigunguName(),
                        e.getLandDong(),
                        e.getAptName(),
                        e.getBuildYear(),
                        e.getUseAreaType(),
                        e.getMinPrice(),
                        e.getAvgPrice(),
                        e.getMaxPrice(),
                        e.getTradeCount()
                ))
                .toList();
    }

    /**
     * 전세 TOP5 아파트 조회 (2026년, rank_type=0, rent_gbn='0')
     */
    public List<Top5LeaseDto> fetchLeaseTop5(String sigunguCode, String useAreaType) {
        QRankUatypeSigunguLease q = QRankUatypeSigunguLease.rankUatypeSigunguLease;

        return queryFactory
                .selectFrom(q)
                .where(
                        q.sigunguCode.eq(sigunguCode),
                        q.useAreaType.eq(useAreaType),
                        q.dealYear.eq("2026"),
                        q.rankType.eq(0),
                        q.rentGbn.eq("0")
                )
                .orderBy(q.avgDeposit.desc())
                .limit(5)
                .fetch()
                .stream()
                .map(e -> new Top5LeaseDto(
                        e.getDealYear(),
                        e.getSigunguName(),
                        e.getLandDong(),
                        e.getAptName(),
                        e.getBuildYear(),
                        e.getUseAreaType(),
                        e.getMinDeposit(),
                        e.getAvgDeposit(),
                        e.getMaxDeposit(),
                        e.getTradeCount()
                ))
                .toList();
    }
}
