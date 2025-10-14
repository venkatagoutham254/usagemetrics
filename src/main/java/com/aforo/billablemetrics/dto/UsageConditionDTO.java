package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.DimensionDefinition;

import lombok.Data;

@Data
public class UsageConditionDTO {

    private DimensionDefinition dimension;
    private String operator;
    private String value;       // ✅ This remains unchanged

    // Do NOT expose DataType here — it will be derived in service
    
    // Explicit getters for GitHub Actions compatibility
    public DimensionDefinition getDimension() { return dimension; }
    public String getOperator() { return operator; }
    public String getValue() { return value; }
    
    // Explicit setters for GitHub Actions compatibility
    public void setDimension(DimensionDefinition dimension) { this.dimension = dimension; }
    public void setOperator(String operator) { this.operator = operator; }
    public void setValue(String value) { this.value = value; }
}
