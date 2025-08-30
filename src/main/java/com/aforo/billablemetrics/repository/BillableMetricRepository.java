package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.BillableMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillableMetricRepository extends JpaRepository<BillableMetric, Long> {
    List<BillableMetric> findByProductId(Long productId); // 🔑 NEW METHOD
}
