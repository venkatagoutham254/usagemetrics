package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import com.aforo.billablemetrics.exception.ResourceNotFoundException;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import com.aforo.billablemetrics.webclient.RatePlanServiceClient;
import com.aforo.billablemetrics.enums.*;
import com.aforo.billablemetrics.util.*;
import com.aforo.billablemetrics.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BillableMetricServiceImpl implements BillableMetricService {

    private final BillableMetricRepository metricRepo;
    private final BillableMetricMapper mapper;
    private final ProductServiceClient productClient;
    private final RatePlanServiceClient ratePlanServiceClient;

    @Override
    public BillableMetricResponse createMetric(CreateBillableMetricRequest request) {
        Long orgId = TenantContext.require();

        if (request.getProductId() != null) {
            validateProductExists(request.getProductId());
            validateProductActive(request.getProductId());
            if (request.getUnitOfMeasure() != null) {
                validateUOMMatchesProductType(request.getProductId(), request.getUnitOfMeasure());
            }
        }

        BillableMetric metric = mapper.toEntity(request);
        metric.setOrganizationId(orgId); // 🔑 set tenant

        if (request.getUsageConditions() != null && !request.getUsageConditions().isEmpty()) {
            List<UsageCondition> enriched = enrichUsageConditions(request.getUsageConditions(), metric);
            if (metric.getAggregationFunction() != null && metric.getAggregationWindow() != null) {
                BillableMetricValidator.validateAll(metric.getUnitOfMeasure(),
                                                    metric.getAggregationFunction(),
                                                    metric.getAggregationWindow(),
                                                    enriched);
            }
            // Ensure mutable list for JPA-managed collection
            metric.setUsageConditions(new ArrayList<>(enriched));
        }

        BillableMetric saved = metricRepo.save(metric);
        return buildResponse(saved);
    }

    @Override
    public BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request) {
        Long orgId = TenantContext.require();

        BillableMetric existing = metricRepo.findByBillableMetricIdAndOrganizationId(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));

        if (request.getProductId() != null) {
            validateProductExists(request.getProductId());
            UnitOfMeasure uomToCheck = request.getUnitOfMeasure() != null
                    ? request.getUnitOfMeasure()
                    : existing.getUnitOfMeasure();
            validateUOMMatchesProductType(request.getProductId(), uomToCheck);
        }

        mapper.updateEntityFromDto(request, existing);

        if (request.getUsageConditions() != null) {
            // If empty list explicitly provided, clear all usage conditions
            if (request.getUsageConditions().isEmpty()) {
                if (existing.getUsageConditions() == null) {
                    existing.setUsageConditions(new ArrayList<>());
                }
                existing.getUsageConditions().clear();
            } else {
                // Process the new usage conditions from the request
                // This replaces all existing conditions with the new ones, allowing multiple conditions with the same dimension name
                List<UsageConditionDTO> newConditions = new ArrayList<>();
                
                int existingCount = existing.getUsageConditions() == null ? 0 : existing.getUsageConditions().size();

                // Process each condition from the request
                for (UsageConditionDTO patch : request.getUsageConditions()) {
                    DimensionDefinition targetDim = patch.getDimension();
                    if (targetDim == null) {
                        // If no dimension provided, allow update only when exactly one existing condition
                        if (existingCount == 1) {
                            // Get the single existing dimension
                            targetDim = existing.getUsageConditions().get(0).getDimension();
                        } else {
                            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                    "usageConditions[].dimension is required when multiple conditions exist");
                        }
                    }

                    // Create a new condition DTO with complete information
                    UsageConditionDTO newCondition = new UsageConditionDTO();
                    newCondition.setDimension(targetDim);
                    
                    // Set operator and value from patch (these can be null/blank, will be handled by enrichUsageConditions)
                    newCondition.setOperator(patch.getOperator());
                    newCondition.setValue(patch.getValue());

                    // Add the new condition to the list (allows multiple with same dimension)
                    // The enrichUsageConditions method will handle setting defaults for missing operator/value
                    newConditions.add(newCondition);
                }

                // Validate and enrich all DTOs against UOM and operators
                List<UsageCondition> updated = enrichUsageConditions(newConditions, existing);

                if (!updated.isEmpty()
                        && existing.getAggregationFunction() != null
                        && existing.getAggregationWindow() != null) {
                    BillableMetricValidator.validateAll(existing.getUnitOfMeasure(),
                            existing.getAggregationFunction(),
                            existing.getAggregationWindow(),
                            updated);
                }

                if (existing.getUsageConditions() == null) {
                    existing.setUsageConditions(new ArrayList<>());
                }
                // Modify the managed collection in-place
                existing.getUsageConditions().clear();
                existing.getUsageConditions().addAll(updated);
            }
        }

        BillableMetric saved = metricRepo.save(existing);
        return buildResponse(saved);
    }

    @Override
    public BillableMetricResponse finalizeMetric(Long id) {
        Long orgId = TenantContext.require();

        BillableMetric metric = metricRepo.findByBillableMetricIdAndOrganizationId(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));

        if (metric.getMetricName() == null || metric.getMetricName().isBlank())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "metricName is required");
        if (metric.getProductId() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "productId is required");
        validateProductActive(metric.getProductId());
        if (metric.getUnitOfMeasure() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "unitOfMeasure is required");

        if (metric.getBillingCriteria() == BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS) {
            if (metric.getUsageConditions() == null || metric.getUsageConditions().isEmpty())
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "usageConditions are required when billingCriteria=BILL_BASED_ON_USAGE_CONDITIONS");
        }

        // Only validate aggregation fields if all are provided
        if (metric.getAggregationFunction() != null && metric.getAggregationWindow() != null) {
            BillableMetricValidator.validateAll(
                metric.getUnitOfMeasure(),
                metric.getAggregationFunction(),
                metric.getAggregationWindow(),
                metric.getUsageConditions() == null ? List.of() : metric.getUsageConditions()
            );
        }

        metric.setStatus(MetricStatus.ACTIVE);
        BillableMetric saved = metricRepo.save(metric);
        return buildResponse(saved);
    }

    @Override
    public BillableMetricResponse getMetricById(Long id) {
        Long orgId = TenantContext.require();
        BillableMetric metric = metricRepo.findByBillableMetricIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        return buildResponse(metric);
    }

    @Override
    public List<BillableMetricResponse> getAllMetrics() {
        Long orgId = TenantContext.require();
        return metricRepo.findByOrganizationId(orgId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMetric(Long id) {
        Long orgId = TenantContext.require();
        BillableMetric metric = metricRepo.findByBillableMetricIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        metricRepo.delete(metric);
        // Best-effort cascade: notify Rate Plan service to remove related rate plans
        try {
            ratePlanServiceClient.deleteByBillableMetricId(id);
        } catch (Exception ex) {
            // Do not block deletion on downstream cleanup issues
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillableMetricResponse> getMetricsByProductId(Long productId) {
        Long orgId = TenantContext.require();
        return metricRepo.findByOrganizationIdAndProductId(orgId, productId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMetricsByProductId(Long productId) {
        Long orgId = TenantContext.require();
        metricRepo.deleteByOrganizationIdAndProductId(orgId, productId);
    }

    // ----------- private helpers unchanged (validateProductExists, etc.) -----------
    private void validateProductExists(Long productId) {
        if (!productClient.productExists(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + productId);
        }
    }

    private void validateProductActive(Long productId) {
        if (!productClient.isProductActive(productId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Product must be ACTIVE to create or finalize billable metrics");
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
            // Basic validation to avoid NPEs and provide clear feedback
            if (dto.getDimension() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "usageConditions[].dimension is required");
            }

            DimensionDefinition dimEnum = dto.getDimension();
            if (!dimEnum.getUom().equalsIgnoreCase(uom.name().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dimension " + dimEnum.getDimension() + " is not valid for UnitOfMeasure: " + uom);
            }

            // Set default operator if not provided
            String operator = dto.getOperator();
            if (operator == null || operator.isBlank()) {
                operator = getDefaultOperatorForDimension(dimEnum);
            }

            // Set default value if not provided
            String value = dto.getValue();
            if (value == null || value.isBlank()) {
                value = getDefaultValueForDimension(dimEnum);
            }

            // Validate the operator (either provided or default)
            if (!UnitOfMeasureValidator.isValidOperatorForDimension(dimEnum, operator)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Operator '" + operator + "' is not valid for dimension " + dimEnum.getDimension());
            }

            // Create the usage condition with the final values
            UsageCondition condition = new UsageCondition();
            condition.setDimension(dimEnum);
            condition.setOperator(operator);
            condition.setValue(value);
            condition.setBillableMetric(metric);
            condition.setType(dimEnum.getType());
            return condition;
        }).toList();
    }

    private String getDefaultOperatorForDimension(DimensionDefinition dimension) {
        // Return the first valid operator for the dimension as default
        List<String> validOperators = dimension.getValidOperators();
        if (validOperators != null && !validOperators.isEmpty()) {
            return validOperators.get(0);
        }
        return "equals"; // fallback default
    }

    private String getDefaultValueForDimension(DimensionDefinition dimension) {
        // Provide sensible default values based on dimension type
        return switch (dimension.getType()) {
            case NUMBER -> "0";
            case STRING -> "";
            case BOOLEAN -> "true";
            case DATE -> "2024-01-01";
            case ENUM -> "default";
            default -> "";
        };
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
}
