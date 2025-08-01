package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.entity.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BillableMetricMapper {
    BillableMetric toEntity(CreateBillableMetricRequest request);

    void updateEntityFromDto(UpdateBillableMetricRequest request, @MappingTarget BillableMetric entity);

    BillableMetricResponse toResponse(BillableMetric entity);

    List<BillableMetricResponse> toResponseList(List<BillableMetric> entities);

    UsageCondition toEntity(UsageConditionDTO dto);

    UsageConditionDTO toDto(UsageCondition entity);

    List<UsageCondition> toUsageConditionEntityList(List<UsageConditionDTO> dtoList);

    List<UsageConditionDTO> toUsageConditionDtoList(List<UsageCondition> entityList);
}
