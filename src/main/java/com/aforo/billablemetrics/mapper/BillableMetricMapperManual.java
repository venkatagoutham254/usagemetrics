package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class BillableMetricMapperManual implements BillableMetricMapper {

    @Override
    public BillableMetric toEntity(CreateBillableMetricRequest request) {
        if (request == null) {
            return null;
        }

        return BillableMetric.builder()
                .productId(request.getProductId())
                .metricName(request.getMetricName())
                .description(request.getDescription())
                .unitOfMeasure(request.getUnitOfMeasure())
                .aggregationFunction(request.getAggregationFunction())
                .aggregationWindow(request.getAggregationWindow())
                .version(request.getVersion())
                .billingCriteria(request.getBillingCriteria())
                .usageConditions(toEntityList(request.getUsageConditions()))
                .build();
    }

    @Override
    public void updateEntityFromDto(UpdateBillableMetricRequest request, BillableMetric entity) {
        if (request == null) {
            return;
        }

        if (request.getProductId() != null) {
            entity.setProductId(request.getProductId());
        }
        if (request.getMetricName() != null) {
            entity.setMetricName(request.getMetricName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getUnitOfMeasure() != null) {
            entity.setUnitOfMeasure(request.getUnitOfMeasure());
        }
        if (request.getAggregationFunction() != null) {
            entity.setAggregationFunction(request.getAggregationFunction());
        }
        if (request.getAggregationWindow() != null) {
            entity.setAggregationWindow(request.getAggregationWindow());
        }
        if (request.getVersion() != null) {
            entity.setVersion(request.getVersion());
        }
        if (request.getBillingCriteria() != null) {
            entity.setBillingCriteria(request.getBillingCriteria());
        }
    }

    @Override
    public BillableMetricResponse toResponse(BillableMetric entity) {
        if (entity == null) {
            return null;
        }

        return BillableMetricResponse.builder()
                .billableMetricId(entity.getBillableMetricId())
                .productId(entity.getProductId())
                .metricName(entity.getMetricName())
                .description(entity.getDescription())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .aggregationFunction(entity.getAggregationFunction())
                .aggregationWindow(entity.getAggregationWindow())
                .version(entity.getVersion())
                .usageConditions(toDtoList(entity.getUsageConditions()))
                .billingCriteria(entity.getBillingCriteria())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .lastUpdated(entity.getLastUpdated())
                .organizationId(entity.getOrganizationId())
                .build();
    }

    @Override
    public List<BillableMetricResponse> toResponseList(List<BillableMetric> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UsageCondition toEntity(UsageConditionDTO dto) {
        if (dto == null) {
            return null;
        }

        UsageCondition entity = new UsageCondition();
        entity.setDimension(dto.getDimension());
        entity.setOperator(dto.getOperator());
        entity.setValue(dto.getValue());
        return entity;
    }

    @Override
    public UsageConditionDTO toDto(UsageCondition entity) {
        if (entity == null) {
            return null;
        }

        UsageConditionDTO dto = new UsageConditionDTO();
        dto.setDimension(entity.getDimension());
        dto.setOperator(entity.getOperator());
        dto.setValue(entity.getValue());
        return dto;
    }

    @Override
    public List<UsageConditionDTO> toDtoList(List<UsageCondition> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
