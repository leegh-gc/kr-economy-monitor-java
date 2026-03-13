package com.kreconomy.monitor.domain.repository;

import com.kreconomy.monitor.domain.entity.StatSigunguYymm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleStatsRepository extends JpaRepository<StatSigunguYymm, Long> {
}
