package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.BillableMetric;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BillableMetricRepository extends JpaRepository<BillableMetric, Long> {
    @EntityGraph(attributePaths = "usageConditions")
    List<BillableMetric> findByProductId(Long productId); // 🔑 NEW METHOD

    @EntityGraph(attributePaths = "usageConditions")
    List<BillableMetric> findByOrganizationId(Long organizationId);

    @EntityGraph(attributePaths = "usageConditions")
    List<BillableMetric> findByOrganizationIdAndProductId(Long organizationId, Long productId);

    @EntityGraph(attributePaths = "usageConditions")
    Optional<BillableMetric> findByBillableMetricIdAndOrganizationId(Long billableMetricId, Long organizationId);

    void deleteByOrganizationIdAndProductId(Long organizationId, Long productId);

}
