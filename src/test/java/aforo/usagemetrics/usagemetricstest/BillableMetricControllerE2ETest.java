/*package aforo.usagemetrics.usagemetricstest;

import com.aforo.billablemetrics.controller.BillableMetricController;
import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.UsageConditionDTO;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.service.BillableMetricService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-slice tests that:
 *  - Explicitly register the controller (no package scanning surprises)
 *  - Reflect the class-level @RequestMapping to derive BASE path automatically
 
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(BillableMetricControllerE2ETest.TestConfig.class)
class BillableMetricControllerE2ETest {

    @Configuration
    static class TestConfig {
        @Bean
        BillableMetricController billableMetricController(BillableMetricService service) {
            return new BillableMetricController(service);
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BillableMetricService service;

    private static String BASE;

    @BeforeAll
    static void resolveBasePath() {
        RequestMapping rm = BillableMetricController.class.getAnnotation(RequestMapping.class);
        BASE = Optional.ofNullable(rm)
                .filter(a -> a.value().length > 0)
                .map(a -> a.value()[0])
                .orElse(""); // falls back to empty if no class-level mapping
        if (!BASE.startsWith("/")) BASE = "/" + BASE;
    }

    private String validCreateJson() {
        return """
        {
          "metricName": "API Requests",
          "productId": 100,
          "version": "v1",
          "unitOfMeasure": "API_CALL",
          "description": "Count of API calls",
          "aggregationFunction": "COUNT",
          "aggregationWindow": "PER_DAY",
          "usageConditions": [
            { "dimension": "STATUS_CODE", "operator": ">", "value": "200" }
          ],
          "billingCriteria": "BILL_BASED_ON_USAGE_CONDITIONS"
        }
        """;
    }

    private BillableMetricResponse sampleResponse(long id) {
        UsageConditionDTO cond = new UsageConditionDTO();
        cond.setDimension(DimensionDefinition.STATUS_CODE);
        cond.setOperator(">");
        cond.setValue("200");

        return BillableMetricResponse.builder()
                .billableMetricId(id)
                .productId(100L)
                .metricName("API Requests")
                .version("v1")
                .unitOfMeasure(UnitOfMeasure.API_CALL)
                .description("Count of API calls")
                .aggregationFunction(AggregationFunction.COUNT)
                .aggregationWindow(AggregationWindow.PER_DAY)
                .usageConditions(List.of(cond))
                .billingCriteria(BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS)
                .build();
    }

    // --------------------- CREATE ---------------------

    @Test
    void create_returns200_and_body() throws Exception {
        when(service.createMetric(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.billableMetricId").value(1));

        verify(service).createMetric(any());
    }

    @Test
    void create_badRequest_400() throws Exception {
        when(service.createMetric(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId"));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJson()))
            .andExpect(status().isBadRequest());

        verify(service).createMetric(any());
    }

    // --------------------- UPDATE ---------------------

    @Test
    void update_returns200_and_body() throws Exception {
        when(service.updateMetric(eq(10L), any())).thenReturn(sampleResponse(10L));

        mockMvc.perform(put(BASE + "/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.billableMetricId").value(10));

        verify(service).updateMetric(eq(10L), any());
    }

    // --------------------- GET by ID ---------------------

    @Test
    void getById_returns200() throws Exception {
        when(service.getMetricById(5L)).thenReturn(sampleResponse(5L));

        mockMvc.perform(get(BASE + "/{id}", 5))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.billableMetricId").value(5));

        verify(service).getMetricById(5L);
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(service.getMetricById(404L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        mockMvc.perform(get(BASE + "/{id}", 404))
            .andExpect(status().isNotFound());
    }

    // --------------------- GET all ---------------------

    @Test
    void getAll_returns200_and_list() throws Exception {
        when(service.getAllMetrics()).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get(BASE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].billableMetricId").value(1))
            .andExpect(jsonPath("$[1].billableMetricId").value(2));

        verify(service).getAllMetrics();
    }

    // --------------------- DELETE ---------------------

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(service).deleteMetric(12L);

        mockMvc.perform(delete(BASE + "/{id}", 12))
            .andExpect(status().isNoContent());

        verify(service).deleteMetric(12L);
    }

    @Test
    void delete_notFound_404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"))
            .when(service).deleteMetric(88L);

        mockMvc.perform(delete(BASE + "/{id}", 88))
            .andExpect(status().isNotFound());
    }
}
*/