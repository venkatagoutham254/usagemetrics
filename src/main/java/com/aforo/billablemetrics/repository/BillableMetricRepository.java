package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.BillableMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillableMetricRepository extends JpaRepository<BillableMetric, Long> {
    // Add custom queries here if needed
}
