package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import com.aforo.billablemetrics.webclient.RatePlanServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillableMetricServiceDebugTest {

    @Mock
    private BillableMetricRepository metricRepo;

    @Mock
    private BillableMetricMapper mapper;

    @Mock
    private ProductServiceClient productClient;

    @Mock
    private RatePlanServiceClient ratePlanServiceClient;

    @InjectMocks
    private BillableMetricServiceImpl service;

    private CreateBillableMetricRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateBillableMetricRequest();
        validRequest.setMetricName("API Call Metric");
        validRequest.setDescription("Test metric for API calls");
        validRequest.setProductId(1L);
        validRequest.setUnitOfMeasure(UnitOfMeasure.API_CALL);
        validRequest.setAggregationFunction(AggregationFunction.COUNT);
        validRequest.setAggregationWindow(AggregationWindow.PER_HOUR);
    }

    @Test
    void debugTestRejectStringForNumberField() {
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.STATUS_CODE);
        condition.setOperator("=");
        condition.setValue("abc"); // Invalid: string instead of number
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        try {
            service.createMetric(validRequest);
            fail("Expected ResponseStatusException to be thrown");
        } catch (ResponseStatusException e) {
            System.out.println("Exception Status: " + e.getStatusCode());
            System.out.println("Exception Message: " + e.getReason());
            System.out.println("Full Exception: " + e);
            
            // Check if it's the tenant error
            if (e.getReason() != null && e.getReason().contains("Missing tenant")) {
                System.out.println("ERROR: Missing tenant context!");
            }
            
            // Re-throw to see full stack trace
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected exception type: " + e.getClass());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
