package com.aforo.billablemetrics.entity;

import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import com.aforo.billablemetrics.enums.MetricStatus;
import com.aforo.billablemetrics.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillableMetric {

    @Id
    @Column(name = "billable_metric_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billableMetricId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "description")
    private String description;

    @Column(name = "unit_of_measure")
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "aggregation_function")
    @Enumerated(EnumType.STRING)
    private AggregationFunction aggregationFunction;

    @Column(name = "aggregation_window")
    @Enumerated(EnumType.STRING)
    private AggregationWindow aggregationWindow;

    @Column(name = "version")
    private String version;

    @Column(name = "billing_criteria")
    @Enumerated(EnumType.STRING)
    private com.aforo.billablemetrics.enums.BillingCriteria billingCriteria;

    @OneToMany(mappedBy = "billableMetric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsageCondition> usageConditions;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MetricStatus status;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "organization_id", nullable = false)
private Long organizationId;


    @PrePersist
    void prePersist() {
        if (status == null) status = MetricStatus.DRAFT;
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (lastUpdated == null) lastUpdated = createdOn;
    }

    @PreUpdate
    void preUpdate() {
        lastUpdated = LocalDateTime.now();

        
    }
}
