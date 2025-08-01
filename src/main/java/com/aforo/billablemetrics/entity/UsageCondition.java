package com.aforo.billablemetrics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dimension; // field being filtered, e.g., responseTime

    private String operator;  // e.g., >, <, =

    private String value;     // e.g., 100

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private BillableMetric billableMetric;
}
