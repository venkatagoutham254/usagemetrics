package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillableMetricMapper {

    BillableMetric toEntity(CreateBillableMetricRequest request);

    void updateEntityFromDto(UpdateBillableMetricRequest request, @MappingTarget BillableMetric entity);

    // ✅ Explicit mapping from entity.billableMetricId -> response.billableMetricId
    @Mapping(source = "billableMetricId", target = "billableMetricId")
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "metricName", target = "metricName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure")
    @Mapping(source = "aggregationFunction", target = "aggregationFunction")
    @Mapping(source = "aggregationWindow", target = "aggregationWindow")
    @Mapping(source = "version", target = "version")
    @Mapping(source = "usageConditions", target = "usageConditions")
    BillableMetricResponse toResponse(BillableMetric entity);

    List<BillableMetricResponse> toResponseList(List<BillableMetric> entities);

    // ✅ For UsageCondition mapping (handled separately from BillableMetric)
    @Mapping(target = "billableMetric", ignore = true)
    @Mapping(target = "type", ignore = true) // Will be derived from DimensionDefinition
    UsageCondition toEntity(UsageConditionDTO dto);

    UsageConditionDTO toDto(UsageCondition entity);

    List<UsageConditionDTO> toDtoList(List<UsageCondition> entities);

    List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos);
}
