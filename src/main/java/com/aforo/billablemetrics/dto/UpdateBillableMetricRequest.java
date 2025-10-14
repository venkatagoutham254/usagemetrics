package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.UnitOfMeasure;
import com.aforo.billablemetrics.enums.AggregationFunction;
import com.aforo.billablemetrics.enums.AggregationWindow;
import lombok.Data;

import java.util.List;


import com.aforo.billablemetrics.enums.BillingCriteria;
@Data
public class UpdateBillableMetricRequest {

    private String metricName;
    private Long productId;
    private String version; // Optional
    private UnitOfMeasure unitOfMeasure;
    private String description;
    private AggregationFunction aggregationFunction;
    private AggregationWindow aggregationWindow;
    private List<UsageConditionDTO> usageConditions;
    private BillingCriteria billingCriteria;

    // Explicit getters to ensure compilation works
    public String getMetricName() { return metricName; }
    public Long getProductId() { return productId; }
    public String getVersion() { return version; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public String getDescription() { return description; }
    public AggregationFunction getAggregationFunction() { return aggregationFunction; }
    public AggregationWindow getAggregationWindow() { return aggregationWindow; }
    public List<UsageConditionDTO> getUsageConditions() { return usageConditions; }
    public BillingCriteria getBillingCriteria() { return billingCriteria; }

    // Explicit setters
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setVersion(String version) { this.version = version; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public void setDescription(String description) { this.description = description; }
    public void setAggregationFunction(AggregationFunction aggregationFunction) { this.aggregationFunction = aggregationFunction; }
    public void setAggregationWindow(AggregationWindow aggregationWindow) { this.aggregationWindow = aggregationWindow; }
    public void setUsageConditions(List<UsageConditionDTO> usageConditions) { this.usageConditions = usageConditions; }
    public void setBillingCriteria(BillingCriteria billingCriteria) { this.billingCriteria = billingCriteria; }
}
