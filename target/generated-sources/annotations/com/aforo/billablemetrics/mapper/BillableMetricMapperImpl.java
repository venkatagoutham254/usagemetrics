package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.TransactionFormat;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-11T11:36:13+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class BillableMetricMapperImpl implements BillableMetricMapper {

    @Override
    public BillableMetric toEntity(CreateBillableMetricRequest request) {
        if ( request == null ) {
            return null;
        }

        BillableMetric.BillableMetricBuilder billableMetric = BillableMetric.builder();

        billableMetric.name( request.getName() );
        billableMetric.description( request.getDescription() );
        billableMetric.unitOfMeasure( request.getUnitOfMeasure() );
        if ( request.getTransactionFormat() != null ) {
            billableMetric.transactionFormat( Enum.valueOf( TransactionFormat.class, request.getTransactionFormat() ) );
        }
        billableMetric.dataSource( request.getDataSource() );
        billableMetric.filterFieldName( request.getFilterFieldName() );
        billableMetric.filterOperator( request.getFilterOperator() );
        billableMetric.filterValue( request.getFilterValue() );
        billableMetric.logicalCombiner( request.getLogicalCombiner() );
        billableMetric.caseSensitive( request.getCaseSensitive() );
        billableMetric.aggregationFunction( request.getAggregationFunction() );
        billableMetric.aggregationWindow( request.getAggregationWindow() );
        billableMetric.groupByKeys( request.getGroupByKeys() );
        billableMetric.threshold( request.getThreshold() );
        billableMetric.resetBehavior( request.getResetBehavior() );
        billableMetric.apiName( request.getApiName() );
        billableMetric.apiVersion( request.getApiVersion() );
        billableMetric.apiPath( request.getApiPath() );
        billableMetric.apiMethod( request.getApiMethod() );
        billableMetric.llmModel( request.getLlmModel() );
        billableMetric.llmEndpointType( request.getLlmEndpointType() );
        billableMetric.llmVersion( request.getLlmVersion() );
        billableMetric.jobType( request.getJobType() );
        billableMetric.sourceSystem( request.getSourceSystem() );
        billableMetric.targetSystem( request.getTargetSystem() );
        billableMetric.dashboardId( request.getDashboardId() );
        billableMetric.widgetId( request.getWidgetId() );
        billableMetric.appSection( request.getAppSection() );
        billableMetric.interactionType( request.getInteractionType() );
        billableMetric.analyticsUserRole( request.getAnalyticsUserRole() );
        billableMetric.agentId( request.getAgentId() );
        billableMetric.agentVersion( request.getAgentVersion() );
        billableMetric.deployment( request.getDeployment() );
        billableMetric.triggerType( request.getTriggerType() );
        billableMetric.channel( request.getChannel() );
        billableMetric.agentRole( request.getAgentRole() );
        billableMetric.effectiveStartDate( request.getEffectiveStartDate() );
        billableMetric.effectiveEndDate( request.getEffectiveEndDate() );
        billableMetric.isEnabled( request.getIsEnabled() );
        billableMetric.priority( request.getPriority() );
        billableMetric.auditLogId( request.getAuditLogId() );
        billableMetric.schemaValidationType( request.getSchemaValidationType() );
        billableMetric.processingType( request.getProcessingType() );
        billableMetric.errorHandling( request.getErrorHandling() );
        billableMetric.performanceSla( request.getPerformanceSla() );
        billableMetric.dryRunMode( request.getDryRunMode() );

        return billableMetric.build();
    }

    @Override
    public void updateEntityFromDto(UpdateBillableMetricRequest request, BillableMetric entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setDescription( request.getDescription() );
        entity.setUnitOfMeasure( request.getUnitOfMeasure() );
        if ( request.getTransactionFormat() != null ) {
            entity.setTransactionFormat( Enum.valueOf( TransactionFormat.class, request.getTransactionFormat() ) );
        }
        else {
            entity.setTransactionFormat( null );
        }
        entity.setDataSource( request.getDataSource() );
        entity.setFilterFieldName( request.getFilterFieldName() );
        entity.setFilterOperator( request.getFilterOperator() );
        entity.setFilterValue( request.getFilterValue() );
        entity.setLogicalCombiner( request.getLogicalCombiner() );
        entity.setCaseSensitive( request.getCaseSensitive() );
        entity.setAggregationFunction( request.getAggregationFunction() );
        entity.setAggregationWindow( request.getAggregationWindow() );
        entity.setGroupByKeys( request.getGroupByKeys() );
        entity.setThreshold( request.getThreshold() );
        entity.setResetBehavior( request.getResetBehavior() );
        entity.setApiName( request.getApiName() );
        entity.setApiVersion( request.getApiVersion() );
        entity.setApiPath( request.getApiPath() );
        entity.setApiMethod( request.getApiMethod() );
        entity.setLlmModel( request.getLlmModel() );
        entity.setLlmEndpointType( request.getLlmEndpointType() );
        entity.setLlmVersion( request.getLlmVersion() );
        entity.setJobType( request.getJobType() );
        entity.setSourceSystem( request.getSourceSystem() );
        entity.setTargetSystem( request.getTargetSystem() );
        entity.setDashboardId( request.getDashboardId() );
        entity.setWidgetId( request.getWidgetId() );
        entity.setAppSection( request.getAppSection() );
        entity.setInteractionType( request.getInteractionType() );
        entity.setAnalyticsUserRole( request.getAnalyticsUserRole() );
        entity.setAgentId( request.getAgentId() );
        entity.setAgentVersion( request.getAgentVersion() );
        entity.setDeployment( request.getDeployment() );
        entity.setTriggerType( request.getTriggerType() );
        entity.setChannel( request.getChannel() );
        entity.setAgentRole( request.getAgentRole() );
        entity.setEffectiveStartDate( request.getEffectiveStartDate() );
        entity.setEffectiveEndDate( request.getEffectiveEndDate() );
        entity.setIsEnabled( request.getIsEnabled() );
        entity.setPriority( request.getPriority() );
        entity.setAuditLogId( request.getAuditLogId() );
        entity.setSchemaValidationType( request.getSchemaValidationType() );
        entity.setProcessingType( request.getProcessingType() );
        entity.setErrorHandling( request.getErrorHandling() );
        entity.setPerformanceSla( request.getPerformanceSla() );
        entity.setDryRunMode( request.getDryRunMode() );
    }

    @Override
    public BillableMetricResponse toResponse(BillableMetric entity) {
        if ( entity == null ) {
            return null;
        }

        BillableMetricResponse.BillableMetricResponseBuilder billableMetricResponse = BillableMetricResponse.builder();

        billableMetricResponse.metricId( entity.getMetricId() );
        billableMetricResponse.name( entity.getName() );
        billableMetricResponse.description( entity.getDescription() );
        billableMetricResponse.unitOfMeasure( entity.getUnitOfMeasure() );
        if ( entity.getTransactionFormat() != null ) {
            billableMetricResponse.transactionFormat( entity.getTransactionFormat().name() );
        }
        billableMetricResponse.dataSource( entity.getDataSource() );
        billableMetricResponse.filterFieldName( entity.getFilterFieldName() );
        billableMetricResponse.filterOperator( entity.getFilterOperator() );
        billableMetricResponse.filterValue( entity.getFilterValue() );
        billableMetricResponse.logicalCombiner( entity.getLogicalCombiner() );
        billableMetricResponse.caseSensitive( entity.getCaseSensitive() );
        billableMetricResponse.aggregationFunction( entity.getAggregationFunction() );
        billableMetricResponse.aggregationWindow( entity.getAggregationWindow() );
        billableMetricResponse.groupByKeys( entity.getGroupByKeys() );
        billableMetricResponse.threshold( entity.getThreshold() );
        billableMetricResponse.resetBehavior( entity.getResetBehavior() );
        billableMetricResponse.apiName( entity.getApiName() );
        billableMetricResponse.apiVersion( entity.getApiVersion() );
        billableMetricResponse.apiPath( entity.getApiPath() );
        billableMetricResponse.apiMethod( entity.getApiMethod() );
        billableMetricResponse.llmModel( entity.getLlmModel() );
        billableMetricResponse.llmEndpointType( entity.getLlmEndpointType() );
        billableMetricResponse.llmVersion( entity.getLlmVersion() );
        billableMetricResponse.jobType( entity.getJobType() );
        billableMetricResponse.sourceSystem( entity.getSourceSystem() );
        billableMetricResponse.targetSystem( entity.getTargetSystem() );
        billableMetricResponse.dashboardId( entity.getDashboardId() );
        billableMetricResponse.widgetId( entity.getWidgetId() );
        billableMetricResponse.appSection( entity.getAppSection() );
        billableMetricResponse.interactionType( entity.getInteractionType() );
        billableMetricResponse.analyticsUserRole( entity.getAnalyticsUserRole() );
        billableMetricResponse.agentId( entity.getAgentId() );
        billableMetricResponse.agentVersion( entity.getAgentVersion() );
        billableMetricResponse.deployment( entity.getDeployment() );
        billableMetricResponse.triggerType( entity.getTriggerType() );
        billableMetricResponse.channel( entity.getChannel() );
        billableMetricResponse.agentRole( entity.getAgentRole() );
        billableMetricResponse.effectiveStartDate( entity.getEffectiveStartDate() );
        billableMetricResponse.effectiveEndDate( entity.getEffectiveEndDate() );
        billableMetricResponse.isEnabled( entity.getIsEnabled() );
        billableMetricResponse.priority( entity.getPriority() );
        billableMetricResponse.auditLogId( entity.getAuditLogId() );
        billableMetricResponse.schemaValidationType( entity.getSchemaValidationType() );
        billableMetricResponse.processingType( entity.getProcessingType() );
        billableMetricResponse.errorHandling( entity.getErrorHandling() );
        billableMetricResponse.performanceSla( entity.getPerformanceSla() );
        billableMetricResponse.dryRunMode( entity.getDryRunMode() );

        return billableMetricResponse.build();
    }
}
