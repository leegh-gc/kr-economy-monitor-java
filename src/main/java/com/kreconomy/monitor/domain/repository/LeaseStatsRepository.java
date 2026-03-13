package com.kreconomy.monitor.domain.repository;

import com.kreconomy.monitor.domain.entity.StatLeaseSigungu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseStatsRepository extends JpaRepository<StatLeaseSigungu, Long> {
}
