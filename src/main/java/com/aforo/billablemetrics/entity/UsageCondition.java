package com.aforo.billablemetrics.entity;

import com.aforo.billablemetrics.enums.*;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DimensionDefinition dimension;

    private String operator; // validated at runtime

    private String value;

    @Transient
    private DataType type; // ✅ Only used internally, not persisted
    
    
    @ManyToOne
    @JoinColumn(name = "billable_metric_id")
    private BillableMetric billableMetric;
    
}
