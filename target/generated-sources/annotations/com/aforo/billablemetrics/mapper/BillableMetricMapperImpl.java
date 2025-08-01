package com.aforo.billablemetrics.mapper;

import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-01T10:08:30+0530",
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

        billableMetric.metricName( request.getMetricName() );
        billableMetric.productId( request.getProductId() );
        billableMetric.version( request.getVersion() );
        billableMetric.unitOfMeasure( request.getUnitOfMeasure() );
        billableMetric.description( request.getDescription() );
        billableMetric.aggregationFunction( request.getAggregationFunction() );
        billableMetric.aggregationWindow( request.getAggregationWindow() );
        billableMetric.usageConditions( toUsageConditionEntityList( request.getUsageConditions() ) );

        return billableMetric.build();
    }

    @Override
    public void updateEntityFromDto(UpdateBillableMetricRequest request, BillableMetric entity) {
        if ( request == null ) {
            return;
        }

        entity.setMetricName( request.getMetricName() );
        entity.setProductId( request.getProductId() );
        entity.setVersion( request.getVersion() );
        entity.setUnitOfMeasure( request.getUnitOfMeasure() );
        entity.setDescription( request.getDescription() );
        entity.setAggregationFunction( request.getAggregationFunction() );
        entity.setAggregationWindow( request.getAggregationWindow() );
        if ( entity.getUsageConditions() != null ) {
            List<UsageCondition> list = toUsageConditionEntityList( request.getUsageConditions() );
            if ( list != null ) {
                entity.getUsageConditions().clear();
                entity.getUsageConditions().addAll( list );
            }
            else {
                entity.setUsageConditions( null );
            }
        }
        else {
            List<UsageCondition> list = toUsageConditionEntityList( request.getUsageConditions() );
            if ( list != null ) {
                entity.setUsageConditions( list );
            }
        }
    }

    @Override
    public BillableMetricResponse toResponse(BillableMetric entity) {
        if ( entity == null ) {
            return null;
        }

        BillableMetricResponse.BillableMetricResponseBuilder billableMetricResponse = BillableMetricResponse.builder();

        billableMetricResponse.metricId( entity.getMetricId() );
        billableMetricResponse.metricName( entity.getMetricName() );
        billableMetricResponse.productId( entity.getProductId() );
        billableMetricResponse.version( entity.getVersion() );
        billableMetricResponse.unitOfMeasure( entity.getUnitOfMeasure() );
        billableMetricResponse.description( entity.getDescription() );
        billableMetricResponse.aggregationFunction( entity.getAggregationFunction() );
        billableMetricResponse.aggregationWindow( entity.getAggregationWindow() );
        billableMetricResponse.usageConditions( toUsageConditionDtoList( entity.getUsageConditions() ) );

        return billableMetricResponse.build();
    }

    @Override
    public List<BillableMetricResponse> toResponseList(List<BillableMetric> entities) {
        if ( entities == null ) {
            return null;
        }

        List<BillableMetricResponse> list = new ArrayList<BillableMetricResponse>( entities.size() );
        for ( BillableMetric billableMetric : entities ) {
            list.add( toResponse( billableMetric ) );
        }

        return list;
    }

    @Override
    public UsageCondition toEntity(UsageConditionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        UsageCondition.UsageConditionBuilder usageCondition = UsageCondition.builder();

        usageCondition.dimension( dto.getDimension() );
        usageCondition.operator( dto.getOperator() );
        usageCondition.value( dto.getValue() );

        return usageCondition.build();
    }

    @Override
    public UsageConditionDTO toDto(UsageCondition entity) {
        if ( entity == null ) {
            return null;
        }

        UsageConditionDTO.UsageConditionDTOBuilder usageConditionDTO = UsageConditionDTO.builder();

        usageConditionDTO.dimension( entity.getDimension() );
        usageConditionDTO.operator( entity.getOperator() );
        usageConditionDTO.value( entity.getValue() );

        return usageConditionDTO.build();
    }

    @Override
    public List<UsageCondition> toUsageConditionEntityList(List<UsageConditionDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<UsageCondition> list = new ArrayList<UsageCondition>( dtoList.size() );
        for ( UsageConditionDTO usageConditionDTO : dtoList ) {
            list.add( toEntity( usageConditionDTO ) );
        }

        return list;
    }

    @Override
    public List<UsageConditionDTO> toUsageConditionDtoList(List<UsageCondition> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<UsageConditionDTO> list = new ArrayList<UsageConditionDTO>( entityList.size() );
        for ( UsageCondition usageCondition : entityList ) {
            list.add( toDto( usageCondition ) );
        }

        return list;
    }
}
