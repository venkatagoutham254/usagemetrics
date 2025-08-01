package com.aforo.billablemetrics.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillableMetricRequest {

    private String metricName;
    private Long productId;
    private String version;
    private String unitOfMeasure;
    private String description;
    private String aggregationFunction;
    private String aggregationWindow;
    private List<UsageConditionDTO> usageConditions;
}
