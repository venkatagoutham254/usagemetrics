package com.aforo.billablemetrics.entity;

import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import com.aforo.billablemetrics.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(mappedBy = "billableMetric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsageCondition> usageConditions;
}
