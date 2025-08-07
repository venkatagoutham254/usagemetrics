package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.UnitOfMeasure;
import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
}
