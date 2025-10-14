package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.UnitOfMeasure;
import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;


import com.aforo.billablemetrics.enums.BillingCriteria;
import com.aforo.billablemetrics.enums.MetricStatus;

@Data
@Builder
public class BillableMetricResponse {

    private Long billableMetricId;
    private String metricName;
    private Long productId;
    private String productName;
    private String version;
    private UnitOfMeasure unitOfMeasure;
    private String description;
    private AggregationFunction aggregationFunction;
    private AggregationWindow aggregationWindow;
    private List<UsageConditionDTO> usageConditions;
    private BillingCriteria billingCriteria;
    private MetricStatus status;
    private LocalDateTime createdOn;
    private LocalDateTime lastUpdated;
    private Long organizationId;

    // Default constructor
    public BillableMetricResponse() {}
    
    // Builder constructor
    @Builder
    public BillableMetricResponse(Long billableMetricId, String metricName, Long productId, String productName,
                                 String version, UnitOfMeasure unitOfMeasure, String description,
                                 AggregationFunction aggregationFunction, AggregationWindow aggregationWindow,
                                 List<UsageConditionDTO> usageConditions, BillingCriteria billingCriteria,
                                 MetricStatus status, LocalDateTime createdOn, LocalDateTime lastUpdated,
                                 Long organizationId) {
        this.billableMetricId = billableMetricId;
        this.metricName = metricName;
        this.productId = productId;
        this.productName = productName;
        this.version = version;
        this.unitOfMeasure = unitOfMeasure;
        this.description = description;
        this.aggregationFunction = aggregationFunction;
        this.aggregationWindow = aggregationWindow;
        this.usageConditions = usageConditions;
        this.billingCriteria = billingCriteria;
        this.status = status;
        this.createdOn = createdOn;
        this.lastUpdated = lastUpdated;
        this.organizationId = organizationId;
    }
}
