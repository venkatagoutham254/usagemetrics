package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.entity.BillableMetric;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BillableMetricMapper {

    BillableMetric toEntity(CreateBillableMetricRequest request);

    void updateEntityFromDto(UpdateBillableMetricRequest request, @MappingTarget BillableMetric entity);

    BillableMetricResponse toResponse(BillableMetric entity);
}
