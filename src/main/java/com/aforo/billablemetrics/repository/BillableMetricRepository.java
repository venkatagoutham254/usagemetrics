package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.BillableMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillableMetricRepository extends JpaRepository<BillableMetric, Long> {
}
