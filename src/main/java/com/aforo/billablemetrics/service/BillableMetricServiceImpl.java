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
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class BillableMetricServiceImpl implements BillableMetricService {

    private final BillableMetricRepository metricRepo;
    private final BillableMetricMapper mapper;
    private final ProductServiceClient productClient;
    private final RatePlanServiceClient ratePlanServiceClient;
    private final com.aforo.billablemetrics.webclient.SubscriptionServiceClient subscriptionServiceClient;

    @Override
    public BillableMetricResponse createMetric(CreateBillableMetricRequest request) {
        Long orgId = TenantContext.require();

        if (request.getProductId() != null) {
            validateProductExists(request.getProductId());
            validateProductReadyForMetrics(request.getProductId());
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
                // Build a map of merged DTOs keyed by dimension, starting from current state
                Map<DimensionDefinition, UsageConditionDTO> merged = new HashMap<>();
                if (existing.getUsageConditions() != null) {
                    for (UsageCondition uc : existing.getUsageConditions()) {
                        UsageConditionDTO dto = mapper.toDto(uc);
                        merged.put(dto.getDimension(), dto);
                    }
                }

                int existingCount = existing.getUsageConditions() == null ? 0 : existing.getUsageConditions().size();

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

                    UsageConditionDTO base = merged.getOrDefault(targetDim, new UsageConditionDTO());
                    // Ensure dimension is set
                    base.setDimension(targetDim);
                    // Apply partial overrides
                    if (patch.getOperator() != null && !patch.getOperator().isBlank()) {
                        base.setOperator(patch.getOperator());
                    }
                    if (patch.getValue() != null && !patch.getValue().isBlank()) {
                        base.setValue(patch.getValue());
                    }

                    // After merge, ensure the condition is complete
                    if (base.getOperator() == null || base.getOperator().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                "usageConditions[].operator is required after merge for dimension: " + targetDim.getDimension());
                    }
                    if (base.getValue() == null || base.getValue().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                "usageConditions[].value is required after merge for dimension: " + targetDim.getDimension());
                    }

                    // Put back
                    merged.put(targetDim, base);
                }

                // Validate and enrich all merged DTOs against UOM and operators
                List<UsageConditionDTO> mergedList = new ArrayList<>(merged.values());
                List<UsageCondition> updated = enrichUsageConditions(mergedList, existing);

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
        // Single round-trip to Product API for existence, readiness and type
        com.aforo.billablemetrics.webclient.ProductServiceClient.ProductResponse prod =
                productClient.getProductLite(metric.getProductId());
        if (prod == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + metric.getProductId());
        String prodStatus = prod.getStatus() == null ? null : prod.getStatus().trim().toUpperCase();
        boolean ready = prodStatus != null && (prodStatus.equals("CONFIGURED") || prodStatus.equals("MEASURED") || prodStatus.equals("PRICED") || prodStatus.equals("LIVE"));
        if (!ready)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Product is not ready to attach billable metrics. Ensure it is configured.");

        if (metric.getUnitOfMeasure() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "unitOfMeasure is required");
        if (metric.getAggregationFunction() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "aggregationFunction is required");
        if (metric.getAggregationWindow() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "aggregationWindow is required");
        if (metric.getVersion() == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "version is required");

        if (metric.getBillingCriteria() == BillingCriteria.BILL_BASED_ON_USAGE_CONDITIONS) {
            if (metric.getUsageConditions() == null || metric.getUsageConditions().isEmpty())
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "usageConditions are required when billingCriteria=BILL_BASED_ON_USAGE_CONDITIONS");
        }

        // Validate against product type using the single fetched product record
        UnitOfMeasure uom = metric.getUnitOfMeasure();
        String productType = normalizeProductType(prod.getProductType());
        if (!UnitOfMeasureValidator.isValidUOMForProductType(productType, uom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "UnitOfMeasure " + uom + " is not valid for ProductType: " + productType);
        }

        BillableMetricValidator.validateAll(
                metric.getUnitOfMeasure(),
                metric.getAggregationFunction(),
                metric.getAggregationWindow(),
                metric.getUsageConditions() == null ? List.of() : metric.getUsageConditions()
        );

        // Explicit finalize moves lifecycle from DRAFT -> CONFIGURED
        metric.setStatus(MetricStatus.CONFIGURED);
        BillableMetric saved = metricRepo.save(metric);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillableMetricResponse getMetricById(Long id) {
        Long orgId = TenantContext.require();
        BillableMetric metric = metricRepo.findByBillableMetricIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        return buildResponse(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillableMetricResponse> getAllMetrics() {
        Long orgId = TenantContext.require();
        return metricRepo.findByOrganizationId(orgId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
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
                .map(this::buildResponse)
                .toList();
    }

    @Override
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

    private void validateProductReadyForMetrics(Long productId) {
        if (!productClient.isProductReadyForMetrics(productId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Product is not ready to attach billable metrics. Ensure it is configured.");
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
            if (dto.getOperator() == null || dto.getOperator().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "usageConditions[].operator is required");
            }
            if (dto.getValue() == null || dto.getValue().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "usageConditions[].value is required");
            }

            DimensionDefinition dimEnum = dto.getDimension();
            if (!dimEnum.getUom().equalsIgnoreCase(uom.name().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dimension " + dimEnum.getDimension() + " is not valid for UnitOfMeasure: " + uom);
            }
            if (!UnitOfMeasureValidator.isValidOperatorForDimension(dimEnum, dto.getOperator())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Operator '" + dto.getOperator() + "' is not valid for dimension " + dimEnum.getDimension());
            }
            UsageCondition condition = mapper.toEntity(dto);
            condition.setBillableMetric(metric);
            condition.setType(dimEnum.getType());
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
        if (metric.getProductId() != null) {
            response.setProductName(productClient.getProductNameById(metric.getProductId()));
        }
        // Derive effective status based on persisted status + external signals
        response.setStatus(deriveStatusFor(metric));
        return response;
    }

    private MetricStatus deriveStatusFor(BillableMetric metric) {
        MetricStatus stored = metric.getStatus() == null ? MetricStatus.DRAFT : metric.getStatus();
        if (stored == MetricStatus.DRAFT) return MetricStatus.DRAFT;

        Long metricId = metric.getBillableMetricId();
        Long productId = metric.getProductId();

        boolean hasActiveRatePlan = false;
        try {
            Long orgId = metric.getOrganizationId();
            hasActiveRatePlan = ratePlanServiceClient.hasActiveRatePlanForMetric(productId, metricId, orgId);
        } catch (Exception ignored) { hasActiveRatePlan = false; }
        if (!hasActiveRatePlan) return MetricStatus.CONFIGURED;

        boolean hasActiveSubscription = false;
        try {
            hasActiveSubscription = subscriptionServiceClient.hasActiveSubscriptionForProduct(productId);
        } catch (Exception ignored) { hasActiveSubscription = false; }
        if (!hasActiveSubscription) return MetricStatus.PRICED;

        return MetricStatus.LIVE;
    }
}
