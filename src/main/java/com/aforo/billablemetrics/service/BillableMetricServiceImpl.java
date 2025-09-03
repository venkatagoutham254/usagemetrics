package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import com.aforo.billablemetrics.exception.ResourceNotFoundException;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.repository.UsageConditionRepository;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.util.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillableMetricServiceImpl implements BillableMetricService {

    private final BillableMetricRepository metricRepo;
    private final UsageConditionRepository conditionRepo;
    private final BillableMetricMapper mapper;
    private final ProductServiceClient productClient;

    @Override
    public BillableMetricResponse createMetric(CreateBillableMetricRequest request) {
        // Soft checks only: productId/uom if present
        if (request.getProductId() != null) {
            validateProductExists(request.getProductId());
            validateProductActive(request.getProductId());
            if (request.getUnitOfMeasure() != null) {
                validateUOMMatchesProductType(request.getProductId(), request.getUnitOfMeasure());
            }
        }
    
        BillableMetric metric = mapper.toEntity(request);
    
        // Status default via @PrePersist → DRAFT
        if (request.getUsageConditions() != null && !request.getUsageConditions().isEmpty()) {
            List<UsageCondition> enriched = enrichUsageConditions(request.getUsageConditions(), metric);
            // Only validate if func/window are present; otherwise postpone to finalize
            if (metric.getAggregationFunction() != null && metric.getAggregationWindow() != null) {
                BillableMetricValidator.validateAll(metric.getUnitOfMeasure(),
                                                    metric.getAggregationFunction(),
                                                    metric.getAggregationWindow(),
                                                    enriched);
            }
            metric.setUsageConditions(enriched);
        }
    
        BillableMetric saved = metricRepo.save(metric);
        return buildResponse(saved);
    }
    
    @Override
    public BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request) {
        BillableMetric existing = metricRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
    
        // Only validate product/uom when provided (support partial updates)
        if (request.getProductId() != null) {
            validateProductExists(request.getProductId());
            UnitOfMeasure uomToCheck = request.getUnitOfMeasure() != null ? request.getUnitOfMeasure()
                                                                          : existing.getUnitOfMeasure();
            validateUOMMatchesProductType(request.getProductId(), uomToCheck);
        }
    
        mapper.updateEntityFromDto(request, existing);
    
        // Conditions: null = leave as-is, empty list = clear, non-empty = replace
        if (request.getUsageConditions() != null) {
            conditionRepo.deleteAll(existing.getUsageConditions());
            List<UsageCondition> updated = request.getUsageConditions().isEmpty()
                    ? List.of()
                    : enrichUsageConditions(request.getUsageConditions(), existing);
    
            // Validate only if func/window present now; otherwise defer to finalize
            if (!updated.isEmpty()
                && existing.getAggregationFunction() != null
                && existing.getAggregationWindow() != null) {
                BillableMetricValidator.validateAll(existing.getUnitOfMeasure(),
                                                    existing.getAggregationFunction(),
                                                    existing.getAggregationWindow(),
                                                    updated);
            }
    
            existing.setUsageConditions(updated);
        }
    
        BillableMetric saved = metricRepo.save(existing);
        return buildResponse(saved);
    }
    
    @Override
    public BillableMetricResponse finalizeMetric(Long id) {
        BillableMetric metric = metricRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
    
        // HARD requireds (backend gate)
        if (metric.getMetricName() == null || metric.getMetricName().isBlank())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "metricName is required");
        if (metric.getProductId() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "productId is required");
        // Product must be ACTIVE to finalize the metric
        validateProductActive(metric.getProductId());
        if (metric.getUnitOfMeasure() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "unitOfMeasure is required");
        if (metric.getAggregationFunction() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "aggregationFunction is required");
        if (metric.getAggregationWindow() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "aggregationWindow is required");
        if (metric.getVersion() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "version is required");
    
        // If billing criteria demands conditions → enforce
        if (metric.getBillingCriteria() == BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS) {
            if (metric.getUsageConditions() == null || metric.getUsageConditions().isEmpty())
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "usageConditions are required when billingCriteria=BILL_BASED_ON_USAGE_CONDITIONS");
        }
    
        // Validate combinatorics (UOM ↔ func/window, dimensions/operators)
        BillableMetricValidator.validateAll(
            metric.getUnitOfMeasure(),
            metric.getAggregationFunction(),
            metric.getAggregationWindow(),
            metric.getUsageConditions() == null ? List.of() : metric.getUsageConditions()
        );
    
        // Flip status → ACTIVE
        metric.setStatus(MetricStatus.ACTIVE);
        BillableMetric saved = metricRepo.save(metric);
        return buildResponse(saved);
    }
    


    @Override
    public BillableMetricResponse getMetricById(Long id) {
        BillableMetric metric = metricRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        return buildResponse(metric);
    }

    @Override
    public List<BillableMetricResponse> getAllMetrics() {
        return metricRepo.findAll().stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public void deleteMetric(Long id) {
        if (!metricRepo.existsById(id)) {
            throw new ResourceNotFoundException("Metric not found with ID: " + id);
        }
        metricRepo.deleteById(id);
    }

    // -------------------- PRIVATE HELPERS --------------------

    private void validateProductExists(Long productId) {
        if (!productClient.productExists(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + productId);
        }
    }

    private void validateProductActive(Long productId) {
        if (!productClient.isProductActive(productId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Product must be ACTIVE to create or finalize billable metrics");
        }
    }

private void validateUOMMatchesProductType(Long productId, UnitOfMeasure uom) {
String productType = normalizeProductType(productClient.getProductTypeById(productId));
    if (!UnitOfMeasureValidator.isValidUOMForProductType(productType, uom)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "UnitOfMeasure " + uom + " is not valid for ProductType: " + productType);
    }
}


private List<UsageCondition> enrichUsageConditions(List<UsageConditionDTO> dtos, BillableMetric metric) {
    UnitOfMeasure uom = metric.getUnitOfMeasure();

    return dtos.stream().map(dto -> {
        DimensionDefinition dimEnum = dto.getDimension(); // ✅ Already deserialized by Jackson

        // Validate: UOM match
        if (!dimEnum.getUom().equalsIgnoreCase(uom.name().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dimension " + dimEnum.getDimension() + " is not valid for UnitOfMeasure: " + uom);
        }

        // Validate: Operator match
        if (!UnitOfMeasureValidator.isValidOperatorForDimension(dimEnum, dto.getOperator())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Operator '" + dto.getOperator() + "' is not valid for dimension " + dimEnum.getDimension());
        }

        // ✅ Build condition
        UsageCondition condition = mapper.toEntity(dto);
        condition.setBillableMetric(metric);
        condition.setType(dimEnum.getType()); // Set from enum
        return condition;
    }).toList();
}


private String normalizeProductType(String rawType) {
    if (rawType == null) return null;
    return switch (rawType.toUpperCase()) {
        case "API", "APICALL", "API_CALL" -> "API";
        case "LLM", "LLMTOKEN", "LLM_TOKEN" -> "LLM";
        case "FLATFILE", "FLAT_FILE" -> "FLATFILE";
        case "SQL", "SQLRESULT", "SQL_RESULT" -> "SQL";
        default -> rawType.toUpperCase();
    };
}

    private BillableMetricResponse buildResponse(BillableMetric metric) {
        BillableMetricResponse response = mapper.toResponse(metric);
        response.setProductName(productClient.getProductNameById(metric.getProductId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillableMetricResponse> getMetricsByProductId(Long productId) {
        return metricRepo.findByProductId(productId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
