package com.aforo.billablemetrics.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillableMetricRequest {

    private String name;
    private String description;
    private String unitOfMeasure;
    private String transactionFormat; // changed from TransactionFormat enum to String
    private String dataSource;

    private String filterFieldName;
    private String filterOperator;
    private String filterValue;
    private String logicalCombiner;
    private Boolean caseSensitive;

    private String aggregationFunction;
    private String aggregationWindow;
    private String groupByKeys;
    private Long threshold;
    private String resetBehavior;

    private String apiName;
    private String apiVersion;
    private String apiPath;
    private String apiMethod;

    private String llmModel;
    private String llmEndpointType;
    private String llmVersion;

    private String jobType;
    private String sourceSystem;
    private String targetSystem;

    private String dashboardId;
    private String widgetId;
    private String appSection;
    private String interactionType;
    private String analyticsUserRole; // match the entity field name, not "analyticsUserRole"

    private String agentId;
    private String agentVersion;
    private String deployment;
    private String triggerType;
    private String channel;
    private String agentRole;

    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private Boolean isEnabled;
    private Integer priority;
    private String auditLogId;

    private String schemaValidationType;
    private String processingType;
    private String errorHandling;
    private String performanceSla;
    private Boolean dryRunMode;
}
