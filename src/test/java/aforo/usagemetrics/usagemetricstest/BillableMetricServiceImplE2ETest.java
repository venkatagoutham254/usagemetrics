/*package aforo.usagemetrics.usagemetricstest;


import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.exception.ResourceNotFoundException;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.repository.UsageConditionRepository;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import com.aforo.billablemetrics.service.BillableMetricServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * “End-to-end style” unit tests for BillableMetricServiceImpl.
 * - Real service with mocked repo/mapper/WebClient client
 * - Validates the critical branches: happy paths and validation failures

@ExtendWith(MockitoExtension.class)
class BillableMetricServiceImplE2ETest {

    @Mock private BillableMetricRepository metricRepo;
    @Mock private UsageConditionRepository conditionRepo;
    @Mock private BillableMetricMapper mapper;
    @Mock private ProductServiceClient productClient;

    @InjectMocks private BillableMetricServiceImpl service;

    // ---------------------------- Helpers ----------------------------

    private CreateBillableMetricRequest createReqApiCountPerDay() {
        CreateBillableMetricRequest r = new CreateBillableMetricRequest();
        // relies on Lombok @Data in your DTOs
        r.setMetricName("API Requests");
        r.setProductId(100L);
        r.setVersion("v1");
        r.setUnitOfMeasure(UnitOfMeasure.API_CALL);
        r.setDescription("Count of API calls");
        r.setAggregationFunction(AggregationFunction.COUNT);
        r.setAggregationWindow(AggregationWindow.PER_DAY);
        r.setBillingCriteria(BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS);

        UsageConditionDTO c = new UsageConditionDTO();
        c.setDimension(DimensionDefinition.STATUS_CODE);
        c.setOperator(">");
        c.setValue("200");
        r.setUsageConditions(List.of(c));
        return r;
    }

    private BillableMetric newMetricFromReq(CreateBillableMetricRequest r) {
        BillableMetric e = new BillableMetric();
        e.setBillableMetricId(null);
        e.setMetricName(r.getMetricName());
        e.setProductId(r.getProductId());
        e.setUnitOfMeasure(r.getUnitOfMeasure());
        e.setDescription(r.getDescription());
        e.setAggregationFunction(r.getAggregationFunction());
        e.setAggregationWindow(r.getAggregationWindow());
        e.setVersion(r.getVersion());
        e.setBillingCriteria(r.getBillingCriteria());
        e.setUsageConditions(new ArrayList<>()); // will be replaced by service
        return e;
    }

    private UsageCondition newCondFromDto(UsageConditionDTO dto) {
        UsageCondition uc = new UsageCondition();
        uc.setDimension(dto.getDimension());
        uc.setOperator(dto.getOperator());
        uc.setValue(dto.getValue());
        return uc;
    }

    // ---------------------------- CREATE ----------------------------

    @Test
    void create_success_saves_and_returns_response() {
        CreateBillableMetricRequest req = createReqApiCountPerDay();
        BillableMetric mapped = newMetricFromReq(req);

        // product exists & valid product type for UOM
        when(productClient.productExists(100L)).thenReturn(true);
        when(productClient.getProductTypeById(100L)).thenReturn("API");
        when(productClient.getProductNameById(100L)).thenReturn("MyProduct");

        // mapper: request -> entity
        when(mapper.toEntity(req)).thenReturn(mapped);
        // mapper: each usage condition dto -> entity
        when(mapper.toEntity(any(UsageConditionDTO.class))).thenAnswer(inv -> newCondFromDto(inv.getArgument(0)));

        // repo save returns same entity (simulate generated id if you prefer)
        when(metricRepo.save(any(BillableMetric.class))).thenAnswer(inv -> {
            BillableMetric e = inv.getArgument(0);
            e.setBillableMetricId(1L);
            return e;
        });

        BillableMetricResponse resp = BillableMetricResponse.builder()
                .billableMetricId(1L)
                .productId(100L)
                .metricName("API Requests")
                .version("v1")
                .unitOfMeasure(UnitOfMeasure.API_CALL)
                .description("Count of API calls")
                .aggregationFunction(AggregationFunction.COUNT)
                .aggregationWindow(AggregationWindow.PER_DAY)
                .usageConditions(req.getUsageConditions())
                .billingCriteria(BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS)
                .build();
        when(mapper.toResponse(any(BillableMetric.class))).thenReturn(resp);

        BillableMetricResponse out = service.createMetric(req);

        assertNotNull(out);
        assertEquals(1L, out.getBillableMetricId());
        assertEquals(UnitOfMeasure.API_CALL, out.getUnitOfMeasure());

        verify(productClient).productExists(100L);
        verify(productClient).getProductTypeById(100L);
        verify(metricRepo).save(any(BillableMetric.class));
        verify(mapper).toResponse(any(BillableMetric.class));
        // product name enrichment call
        verify(productClient).getProductNameById(100L);
    }

    @Test
    void create_invalidProductId_returns400() {
        CreateBillableMetricRequest req = createReqApiCountPerDay();
        when(productClient.productExists(100L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createMetric(req));
        assertEquals(400, ex.getStatusCode().value());

        verify(productClient).productExists(100L);
        verifyNoInteractions(metricRepo);
    }

    @Test
    void create_invalidUOM_forProductType_returns400() {
        CreateBillableMetricRequest req = createReqApiCountPerDay();
        // pretend product is LLM; API_CALL is not allowed for LLM
        when(productClient.productExists(100L)).thenReturn(true);
        when(productClient.getProductTypeById(100L)).thenReturn("LLM");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createMetric(req));
        assertEquals(400, ex.getStatusCode().value());

        verify(productClient).getProductTypeById(100L);
        verifyNoInteractions(metricRepo);
    }

// replace the whole test body for "create_invalidOperator_throwsIllegalArgument"
@Test
void create_invalidOperator_returns400() {
    CreateBillableMetricRequest req = createReqApiCountPerDay();
    req.getUsageConditions().get(0).setOperator("BEGINS"); // invalid for STATUS_CODE

    when(productClient.productExists(100L)).thenReturn(true);
    when(productClient.getProductTypeById(100L)).thenReturn("API");

    when(mapper.toEntity(req)).thenReturn(newMetricFromReq(req));
    // IMPORTANT: do NOT stub mapper.toEntity(UsageConditionDTO) here; service will fail before using it.

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createMetric(req));
    assertEquals(400, ex.getStatusCode().value());
}

@Test
void create_dimensionUomMismatch_returns400() {
    CreateBillableMetricRequest req = createReqApiCountPerDay();
    // use a TOKEN dimension while metric UOM = API_CALL
    UsageConditionDTO tokenDim = new UsageConditionDTO();
    tokenDim.setDimension(DimensionDefinition.TOKEN_COUNT);
    tokenDim.setOperator(">");
    tokenDim.setValue("10");
    req.setUsageConditions(List.of(tokenDim));

    when(productClient.productExists(100L)).thenReturn(true);
    when(productClient.getProductTypeById(100L)).thenReturn("API");
    when(mapper.toEntity(req)).thenReturn(newMetricFromReq(req));
    // ❌ remove: when(mapper.toEntity(any(UsageConditionDTO.class)))...

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createMetric(req));
    assertEquals(400, ex.getStatusCode().value());
}


    // ---------------------------- UPDATE ----------------------------

    @Test
    void update_success_merges_and_saves() {
        long id = 50L;

        // existing entity
        BillableMetric existing = new BillableMetric();
        existing.setBillableMetricId(id);
        existing.setProductId(100L);
        existing.setUnitOfMeasure(UnitOfMeasure.API_CALL);
        existing.setUsageConditions(new ArrayList<>());

        when(metricRepo.findById(id)).thenReturn(Optional.of(existing));

        UpdateBillableMetricRequest req = new UpdateBillableMetricRequest();
        req.setProductId(100L);
        req.setUnitOfMeasure(UnitOfMeasure.API_CALL);
        req.setMetricName("Updated Name");
        req.setAggregationFunction(AggregationFunction.COUNT);
        req.setAggregationWindow(AggregationWindow.PER_DAY);
        req.setBillingCriteria(BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS);

        UsageConditionDTO c = new UsageConditionDTO();
        c.setDimension(DimensionDefinition.STATUS_CODE);
        c.setOperator(">=");
        c.setValue("400");
        req.setUsageConditions(List.of(c));

        when(productClient.productExists(100L)).thenReturn(true);
        when(productClient.getProductTypeById(100L)).thenReturn("API");
        when(mapper.toEntity(any(UsageConditionDTO.class))).thenAnswer(inv -> newCondFromDto(inv.getArgument(0)));

        // simulate in-place patch by mapper
        doAnswer(inv -> {
            UpdateBillableMetricRequest r = inv.getArgument(0);
            BillableMetric e = inv.getArgument(1);
            e.setMetricName(r.getMetricName());
            e.setAggregationFunction(r.getAggregationFunction());
            e.setAggregationWindow(r.getAggregationWindow());
            e.setBillingCriteria(r.getBillingCriteria());
            return null;
        }).when(mapper).updateEntityFromDto(eq(req), eq(existing));

        when(metricRepo.save(existing)).thenReturn(existing);

        BillableMetricResponse out = BillableMetricResponse.builder()
                .billableMetricId(id)
                .productId(100L)
                .metricName("Updated Name")
                .version(existing.getVersion())
                .unitOfMeasure(UnitOfMeasure.API_CALL)
                .description(existing.getDescription())
                .aggregationFunction(AggregationFunction.COUNT)
                .aggregationWindow(AggregationWindow.PER_DAY)
                .usageConditions(req.getUsageConditions())
                .billingCriteria(BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS)
                .build();
        when(mapper.toResponse(existing)).thenReturn(out);

        BillableMetricResponse resp = service.updateMetric(id, req);

        assertNotNull(resp);
        assertEquals(id, resp.getBillableMetricId());
        assertEquals("Updated Name", resp.getMetricName());

        verify(conditionRepo).deleteAll(anyList()); // old conditions removed
        verify(metricRepo).save(existing);
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(metricRepo.findById(999L)).thenReturn(Optional.empty());
        UpdateBillableMetricRequest req = new UpdateBillableMetricRequest();
        req.setProductId(100L);
        req.setUnitOfMeasure(UnitOfMeasure.API_CALL);

        assertThrows(ResourceNotFoundException.class, () -> service.updateMetric(999L, req));
        verifyNoMoreInteractions(metricRepo);
    }

    // ---------------------------- GET / DELETE ----------------------------

    @Test
    void getById_found_returnsResponse() {
        BillableMetric e = new BillableMetric();
        e.setBillableMetricId(7L);
        e.setProductId(100L);
        e.setUnitOfMeasure(UnitOfMeasure.API_CALL);

        when(metricRepo.findById(7L)).thenReturn(Optional.of(e));
        when(mapper.toResponse(e)).thenReturn(BillableMetricResponse.builder()
                .billableMetricId(7L).productId(100L).unitOfMeasure(UnitOfMeasure.API_CALL).build());

        assertEquals(7L, service.getMetricById(7L).getBillableMetricId());
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(metricRepo.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMetricById(404L));
    }

    @Test
    void delete_found_deletes() {
        BillableMetric e = new BillableMetric();
        e.setBillableMetricId(12L);
        when(metricRepo.existsById(12L)).thenReturn(true);

        service.deleteMetric(12L);

        verify(metricRepo).deleteById(12L);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(metricRepo.existsById(55L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteMetric(55L));
        verify(metricRepo, never()).deleteById(anyLong());
    }
}
*/
