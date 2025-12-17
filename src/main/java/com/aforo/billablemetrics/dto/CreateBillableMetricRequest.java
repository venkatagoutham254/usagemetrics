package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.UnitOfMeasure;
import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import lombok.Data;

import java.util.List;


import com.aforo.billablemetrics.enums.BillingCriteria;
@Data
public class CreateBillableMetricRequest {

    private String metricName;

    private Long productId;

    private String version; // Optional

    private UnitOfMeasure unitOfMeasure;

    private String description;

    private AggregationFunction aggregationFunction;

    private AggregationWindow aggregationWindow;

    private List<UsageConditionDTO> usageConditions;

    private BillingCriteria billingCriteria;
}
