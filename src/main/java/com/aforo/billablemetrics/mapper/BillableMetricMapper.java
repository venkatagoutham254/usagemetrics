package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BillableMetricMapper {
    
    @Mapping(target = "billableMetricId", ignore = true)
    BillableMetric toEntity(CreateBillableMetricRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usageConditions", ignore = true)
    void updateEntityFromDto(UpdateBillableMetricRequest request, @MappingTarget BillableMetric entity);

    @Mapping(source = "billableMetricId",   target = "billableMetricId")
    @Mapping(source = "productId",          target = "productId")
    @Mapping(source = "metricName",         target = "metricName")
    @Mapping(source = "description",        target = "description")
    @Mapping(source = "unitOfMeasure",      target = "unitOfMeasure")
    @Mapping(source = "aggregationFunction",target = "aggregationFunction")
    @Mapping(source = "aggregationWindow",  target = "aggregationWindow")
    @Mapping(source = "version",            target = "version")
    @Mapping(source = "usageConditions",    target = "usageConditions")
    @Mapping(source = "billingCriteria",    target = "billingCriteria")
    @Mapping(source = "status",             target = "status")
    @Mapping(source = "createdOn",          target = "createdOn")
    @Mapping(source = "lastUpdated",        target = "lastUpdated")
    @Mapping(source = "organizationId",     target = "organizationId") // 👈 NEW
    @Mapping(target = "productName",        ignore = true)
    BillableMetricResponse toResponse(BillableMetric entity);

    List<BillableMetricResponse> toResponseList(List<BillableMetric> entities);

    @Mapping(target = "billableMetric", ignore = true)
    @Mapping(target = "type",           ignore = true)
    @Mapping(target = "id",             ignore = true)
    UsageCondition toEntity(UsageConditionDTO dto);

    UsageConditionDTO toDto(UsageCondition entity);

    List<UsageConditionDTO> toDtoList(List<UsageCondition> entities);

    List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos);
}
