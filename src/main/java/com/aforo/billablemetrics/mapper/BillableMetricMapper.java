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

    // Ignore DB-managed ID on create
    @Mapping(target = "billableMetricId", ignore = true)
    BillableMetric toEntity(CreateBillableMetricRequest request);

    // ✨ NEW: ignore nulls on update (partial PUT) + let service handle usageConditions
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usageConditions", ignore = true) // service decides: null=keep, []=clear, non-[] = replace
    void updateEntityFromDto(UpdateBillableMetricRequest request, @MappingTarget BillableMetric entity);

    // Map entity -> response; productName is enriched in the service
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
    @Mapping(source = "status",             target = "status") // ✨ NEW: map status (DRAFT/ACTIVE) to response
    @Mapping(target = "productName",        ignore = true)
    BillableMetricResponse toResponse(BillableMetric entity);

    List<BillableMetricResponse> toResponseList(List<BillableMetric> entities);

    // UsageCondition mapping – set by service, not by mapper
    @Mapping(target = "billableMetric", ignore = true)
    @Mapping(target = "type",           ignore = true) // derived from DimensionDefinition
    @Mapping(target = "id",             ignore = true) // DB-managed
    UsageCondition toEntity(UsageConditionDTO dto);

    UsageConditionDTO toDto(UsageCondition entity);

    List<UsageConditionDTO> toDtoList(List<UsageCondition> entities);

    List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos);
}
