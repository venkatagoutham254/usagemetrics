package com.aforo.billablemetrics.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillableMetricResponse {

    private Long metricId;
    private String metricName;
    private Long productId;
    private String productName; // populated from product-service
    private String version;
    private String unitOfMeasure;
    private String description;
    private String aggregationFunction;
    private String aggregationWindow;
    private List<UsageConditionDTO> usageConditions;
}
