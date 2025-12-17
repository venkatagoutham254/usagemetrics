package com.aforo.billablemetrics.dto;

import com.aforo.billablemetrics.enums.DimensionDefinition;

import lombok.Data;

@Data
public class UsageConditionDTO {

private DimensionDefinition dimension;
private String operator;
    private String value;       // ✅ This remains unchanged

    // Do NOT expose DataType here — it will be derived in service
}
