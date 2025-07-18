package com.aforo.billablemetrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "billable_metric")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillableMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    // 1. Metric Definition
    private String name;
    private String description;
    private String unitOfMeasure;

    @Enumerated(EnumType.STRING)
    private TransactionFormat transactionFormat;

    private String dataSource; // e.g. api_call, etl_job, etc.

    // 2. Filter / Condition (flattened single condition block)
    private String filterFieldName;
    private String filterOperator;
    private String filterValue;
    private String logicalCombiner; // AND / OR / NOT
    private Boolean caseSensitive;

    // 3. Aggregation & Billing
    private String aggregationFunction;      // COUNT, SUM, etc.
    private String aggregationWindow;        // CALENDAR_MONTH, ROLLING_30D
    private String groupByKeys;              // comma-separated string
    private Long threshold;                  // e.g. bill after 1000
    private String resetBehavior;            // monthly, on-demand

    // 4. Format-specific blocks (API / LLM / Data Exchange / Analytics / AI Agent)

    // API
    private String apiName;
    private String apiVersion;
    private String apiPath;
    private String apiMethod;

    // LLM
    private String llmModel;
    private String llmEndpointType;
    private String llmVersion;

    // Data Exchange
    private String jobType;
    private String sourceSystem;
    private String targetSystem;

    // Embedded Analytics
    private String dashboardId;
    private String widgetId;
    private String appSection;
    private String interactionType;
    private String analyticsUserRole;

    // AI Agent
    private String agentId;
    private String agentVersion;
    private String deployment;
    private String triggerType;
    private String channel;
    private String agentRole;

    // 5. Lifecycle
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;

    private Boolean isEnabled;
    private Integer priority;
    private String auditLogId;

    // 6. Validation / Processing
    private String schemaValidationType; // e.g. JSON, CSV, etc.
    private String processingType;       // real-time / batch
    private String errorHandling;        // drop, retry, alert
    private String performanceSla;       // e.g. ≤ 100 ms

    private Boolean dryRunMode;
}
