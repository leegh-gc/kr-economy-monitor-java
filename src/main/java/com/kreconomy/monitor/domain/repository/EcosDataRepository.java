package com.kreconomy.monitor.domain.repository;

import com.kreconomy.monitor.domain.entity.EcosData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EcosDataRepository extends JpaRepository<EcosData, Long> {

    /**
     * stat_code + item_code1 + date 범위로 조회 (오름차순)
     */
    List<EcosData> findByStatCodeAndItemCode1AndDateGreaterThanEqualAndDateLessThanEqualOrderByDateAsc(
            String statCode, String itemCode1, String dateStart, String dateEnd);

    /**
     * stat_code + item_code1 조합이 DB에 존재하는지 빠른 확인
     */
    boolean existsByStatCodeAndItemCode1(String statCode, String itemCode1);
}
