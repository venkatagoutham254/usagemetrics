package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;

import java.util.List;

public interface BillableMetricMapper{
    
    BillableMetric toEntity(CreateBillableMetricRequest request);

    void updateEntityFromDto(UpdateBillableMetricRequest request, BillableMetric entity);

    BillableMetricResponse toResponse(BillableMetric entity);

    List<BillableMetricResponse> toResponseList(List<BillableMetric> entities);

    UsageCondition toEntity(UsageConditionDTO dto);

    UsageConditionDTO toDto(UsageCondition entity);

    List<UsageConditionDTO> toDtoList(List<UsageCondition> entities);

    List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos);
}
