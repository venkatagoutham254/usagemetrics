package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.tenant.TenantContext;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import com.aforo.billablemetrics.webclient.RatePlanServiceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillableMetricServiceValidationTest {

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
        // Set up tenant context for testing
        TenantContext.set(1L);
        
        validRequest = new CreateBillableMetricRequest();
        validRequest.setMetricName("API Call Metric");
        validRequest.setDescription("Test metric for API calls");
        validRequest.setProductId(1L);
        validRequest.setUnitOfMeasure(UnitOfMeasure.API_CALL);
        validRequest.setAggregationFunction(AggregationFunction.COUNT);
        validRequest.setAggregationWindow(AggregationWindow.PER_HOUR);
    }
    
    @AfterEach
    void tearDown() {
        // Clear tenant context after each test
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should reject string value for NUMBER type field (statusCode)")
    void testRejectStringForNumberField() {
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.STATUS_CODE);
        condition.setOperator("=");
        condition.setValue("abc"); // Invalid: string instead of number
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be a valid number"));
        assertTrue(exception.getMessage().contains("statusCode"));
    }

    @Test
    @DisplayName("Should reject string value for NUMBER type field (responseTime)")
    void testRejectStringForResponseTime() {
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.RESPONSE_TIME);
        condition.setOperator(">");
        condition.setValue("fast"); // Invalid: string instead of number
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be a valid number"));
        assertTrue(exception.getMessage().contains("responseTime"));
    }

    @Test
    @DisplayName("Should reject invalid boolean value")
    void testRejectInvalidBoolean() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.MB);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.COMPRESSED);
        condition.setOperator("is true");
        condition.setValue("yes"); // Invalid: should be true/false
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be either 'true' or 'false'"));
    }

    @Test
    @DisplayName("Should reject invalid date format")
    void testRejectInvalidDate() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.REQUEST);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.TIMESTAMP);
        condition.setOperator("after");
        condition.setValue("not-a-date"); // Invalid date format
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be a valid date"));
    }

    @Test
    @DisplayName("Should reject invalid enum value for region")
    void testRejectInvalidEnumRegion() {
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.REGION_API);
        condition.setOperator("equals");
        condition.setValue("invalid-region"); // Invalid region
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be one of"));
        assertTrue(exception.getMessage().contains("region"));
    }

    @Test
    @DisplayName("Should reject invalid enum value for currency")
    void testRejectInvalidEnumCurrency() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.TRANSACTION);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.CURRENCY);
        condition.setOperator("equals");
        condition.setValue("INVALID"); // Invalid currency
        
        validRequest.setUsageConditions(List.of(condition));
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be one of"));
        assertTrue(exception.getMessage().contains("currency"));
    }

    @Test
    @DisplayName("Should accept valid number for amount field")
    void testAcceptValidNumberForAmount() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.TRANSACTION);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.AMOUNT);
        condition.setOperator(">");
        condition.setValue("100.50"); // Valid number
        
        validRequest.setUsageConditions(List.of(condition));
        
        BillableMetric entity = new BillableMetric();
        // entity.setMetricId(1L); // Entity uses @GeneratedValue, ID is auto-generated
        entity.setProductId(1L);
        entity.setUnitOfMeasure(UnitOfMeasure.TRANSACTION);
        
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(entity);
        when(metricRepo.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(null);
        
        assertDoesNotThrow(() -> service.createMetric(validRequest));
    }

    @Test
    @DisplayName("Should accept valid boolean value")
    void testAcceptValidBoolean() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.QUERY_EXECUTION);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.CACHED);
        condition.setOperator("is true");
        condition.setValue("true"); // Valid boolean
        
        validRequest.setUsageConditions(List.of(condition));
        
        BillableMetric entity = new BillableMetric();
        // entity.setMetricId(1L); // Entity uses @GeneratedValue, ID is auto-generated
        entity.setProductId(1L);
        entity.setUnitOfMeasure(UnitOfMeasure.QUERY_EXECUTION);
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("SQL");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(entity);
        when(metricRepo.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(null);
        
        assertDoesNotThrow(() -> service.createMetric(validRequest));
    }

    @Test
    @DisplayName("Should accept valid date format")
    void testAcceptValidDate() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.FILE);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.UPLOAD_TIME);
        condition.setOperator("after");
        condition.setValue("2024-01-15"); // Valid date
        
        validRequest.setUsageConditions(List.of(condition));
        
        BillableMetric entity = new BillableMetric();
        // entity.setMetricId(1L); // Entity uses @GeneratedValue, ID is auto-generated
        entity.setProductId(1L);
        entity.setUnitOfMeasure(UnitOfMeasure.FILE);
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("FLATFILE");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(entity);
        when(metricRepo.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(null);
        
        assertDoesNotThrow(() -> service.createMetric(validRequest));
    }

    @Test
    @DisplayName("Should accept valid enum value")
    void testAcceptValidEnum() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.HIT);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.DEVICE);
        condition.setOperator("equals");
        condition.setValue("mobile"); // Valid device type
        
        validRequest.setUsageConditions(List.of(condition));
        
        BillableMetric entity = new BillableMetric();
        // entity.setMetricId(1L); // Entity uses @GeneratedValue, ID is auto-generated
        entity.setProductId(1L);
        entity.setUnitOfMeasure(UnitOfMeasure.HIT);
        
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.isProductActive(1L)).thenReturn(true);
        when(productClient.getProductTypeById(1L)).thenReturn("API");
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(entity);
        when(metricRepo.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(null);
        
        assertDoesNotThrow(() -> service.createMetric(validRequest));
    }

    @Test
    @DisplayName("Should reject multiple invalid conditions")
    void testRejectMultipleInvalidConditions() {
        UsageConditionDTO condition1 = new UsageConditionDTO();
        condition1.setDimension(DimensionDefinition.STATUS_CODE);
        condition1.setOperator("=");
        condition1.setValue("not-a-number"); // Invalid
        
        UsageConditionDTO condition2 = new UsageConditionDTO();
        condition2.setDimension(DimensionDefinition.RESPONSE_TIME);
        condition2.setOperator(">");
        condition2.setValue("slow"); // Invalid
        
        validRequest.setUsageConditions(Arrays.asList(condition1, condition2));
        
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(new BillableMetric());
        
        // Should fail on first invalid condition
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createMetric(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("must be a valid number"));
    }

    @Test
    @DisplayName("Should normalize enum values to standard format")
    void testNormalizeEnumValues() {
        validRequest.setUnitOfMeasure(UnitOfMeasure.TRANSACTION);
        
        UsageConditionDTO condition = new UsageConditionDTO();
        condition.setDimension(DimensionDefinition.CURRENCY);
        condition.setOperator("equals");
        condition.setValue("usd"); // lowercase, should be normalized to USD
        
        validRequest.setUsageConditions(List.of(condition));
        
        BillableMetric entity = new BillableMetric();
        // entity.setMetricId(1L); // Entity uses @GeneratedValue, ID is auto-generated
        entity.setProductId(1L);
        entity.setUnitOfMeasure(UnitOfMeasure.TRANSACTION);
        
        when(productClient.getProductNameById(1L)).thenReturn("Test Product");
        when(mapper.toEntity(any(CreateBillableMetricRequest.class))).thenReturn(entity);
        when(metricRepo.save(any())).thenAnswer(invocation -> {
            BillableMetric saved = invocation.getArgument(0);
            // Verify the value was normalized to uppercase
            assertEquals(1, saved.getUsageConditions().size());
            assertEquals("USD", saved.getUsageConditions().get(0).getValue());
            return saved;
        });
        when(mapper.toResponse(any())).thenReturn(null);
        
        assertDoesNotThrow(() -> service.createMetric(validRequest));
        
        verify(metricRepo).save(any());
    }
}
