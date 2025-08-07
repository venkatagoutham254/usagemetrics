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
    date = "2025-08-07T14:12:32+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class BillableMetricMapperImpl implements BillableMetricMapper {

    @Override
    public BillableMetric toEntity(CreateBillableMetricRequest request) {
        if ( request == null ) {
            return null;
        }

        BillableMetric.BillableMetricBuilder billableMetric = BillableMetric.builder();

        billableMetric.productId( request.getProductId() );
        billableMetric.metricName( request.getMetricName() );
        billableMetric.description( request.getDescription() );
        billableMetric.unitOfMeasure( request.getUnitOfMeasure() );
        billableMetric.aggregationFunction( request.getAggregationFunction() );
        billableMetric.aggregationWindow( request.getAggregationWindow() );
        billableMetric.version( request.getVersion() );
        billableMetric.usageConditions( toEntityList( request.getUsageConditions() ) );

        return billableMetric.build();
    }

    @Override
    public void updateEntityFromDto(UpdateBillableMetricRequest request, BillableMetric entity) {
        if ( request == null ) {
            return;
        }

        entity.setProductId( request.getProductId() );
        entity.setMetricName( request.getMetricName() );
        entity.setDescription( request.getDescription() );
        entity.setUnitOfMeasure( request.getUnitOfMeasure() );
        entity.setAggregationFunction( request.getAggregationFunction() );
        entity.setAggregationWindow( request.getAggregationWindow() );
        entity.setVersion( request.getVersion() );
        if ( entity.getUsageConditions() != null ) {
            List<UsageCondition> list = toEntityList( request.getUsageConditions() );
            if ( list != null ) {
                entity.getUsageConditions().clear();
                entity.getUsageConditions().addAll( list );
            }
            else {
                entity.setUsageConditions( null );
            }
        }
        else {
            List<UsageCondition> list = toEntityList( request.getUsageConditions() );
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

        billableMetricResponse.billableMetricId( entity.getBillableMetricId() );
        billableMetricResponse.productId( entity.getProductId() );
        billableMetricResponse.metricName( entity.getMetricName() );
        billableMetricResponse.description( entity.getDescription() );
        billableMetricResponse.unitOfMeasure( entity.getUnitOfMeasure() );
        billableMetricResponse.aggregationFunction( entity.getAggregationFunction() );
        billableMetricResponse.aggregationWindow( entity.getAggregationWindow() );
        billableMetricResponse.version( entity.getVersion() );
        billableMetricResponse.usageConditions( toDtoList( entity.getUsageConditions() ) );

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

        UsageConditionDTO usageConditionDTO = new UsageConditionDTO();

        usageConditionDTO.setDimension( entity.getDimension() );
        usageConditionDTO.setOperator( entity.getOperator() );
        usageConditionDTO.setValue( entity.getValue() );

        return usageConditionDTO;
    }

    @Override
    public List<UsageConditionDTO> toDtoList(List<UsageCondition> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UsageConditionDTO> list = new ArrayList<UsageConditionDTO>( entities.size() );
        for ( UsageCondition usageCondition : entities ) {
            list.add( toDto( usageCondition ) );
        }

        return list;
    }

    @Override
    public List<UsageCondition> toEntityList(List<UsageConditionDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<UsageCondition> list = new ArrayList<UsageCondition>( dtos.size() );
        for ( UsageConditionDTO usageConditionDTO : dtos ) {
            list.add( toEntity( usageConditionDTO ) );
        }

        return list;
    }
}
