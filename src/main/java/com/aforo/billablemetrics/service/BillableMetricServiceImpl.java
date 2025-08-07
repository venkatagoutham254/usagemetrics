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

import java.util.Arrays;
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
    validateProductExists(request.getProductId());
    validateUOMMatchesProductType(request.getProductId(), request.getUnitOfMeasure());

    BillableMetric metric = mapper.toEntity(request);


    if (request.getUsageConditions() != null && !request.getUsageConditions().isEmpty()) {
        List<UsageCondition> enrichedConditions = enrichUsageConditions(request.getUsageConditions(), metric);

        // ✅ Add validation here
        BillableMetricValidator.validateAll(
                request.getUnitOfMeasure(),
                request.getAggregationFunction(),
                request.getAggregationWindow(),
                enrichedConditions
        );

        metric.setUsageConditions(enrichedConditions);
    }

    BillableMetric saved = metricRepo.save(metric);
    return buildResponse(saved);
}

@Override
public BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request) {
    BillableMetric existing = metricRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));

    validateProductExists(request.getProductId());
    validateUOMMatchesProductType(request.getProductId(), request.getUnitOfMeasure());

    mapper.updateEntityFromDto(request, existing);

    conditionRepo.deleteAll(existing.getUsageConditions());
    List<UsageCondition> updatedConditions = enrichUsageConditions(request.getUsageConditions(), existing);

    // ✅ Add validation here
    BillableMetricValidator.validateAll(
            request.getUnitOfMeasure(),
            request.getAggregationFunction(),
            request.getAggregationWindow(),
            updatedConditions
    );

    existing.setUsageConditions(updatedConditions);

    BillableMetric saved = metricRepo.save(existing);
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
}
