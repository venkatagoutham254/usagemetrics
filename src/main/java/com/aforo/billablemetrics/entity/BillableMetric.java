package com.aforo.billablemetrics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillableMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    private String metricName;

    private Long productId; // validated via WebClient

    private String version;

    private String unitOfMeasure;

    private String description;

    private String aggregationFunction;

    private String aggregationWindow;

    @OneToMany(mappedBy = "billableMetric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsageCondition> usageConditions;
}
