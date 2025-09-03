package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.BillableMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BillableMetricRepository extends JpaRepository<BillableMetric, Long> {
    List<BillableMetric> findByProductId(Long productId); // 🔑 NEW METHOD


    List<BillableMetric> findByOrganizationId(Long organizationId);
    List<BillableMetric> findByOrganizationIdAndProductId(Long organizationId, Long productId);


    Optional<BillableMetric> findByBillableMetricIdAndOrganizationId(Long billableMetricId, Long organizationId);

}
