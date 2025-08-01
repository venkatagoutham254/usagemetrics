package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.exception.ResourceNotFoundException;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.UsageCondition;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import com.aforo.billablemetrics.repository.UsageConditionRepository;
import com.aforo.billablemetrics.webclient.ProductServiceClient;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        if (!productClient.productExists(request.getProductId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + request.getProductId());
        }

        BillableMetric metric = mapper.toEntity(request);

        if (request.getUsageConditions() != null) {
            List<UsageCondition> conditions = mapper.toUsageConditionEntityList(request.getUsageConditions());
            conditions.forEach(c -> c.setBillableMetric(metric));
            metric.setUsageConditions(conditions);
        }

        BillableMetric saved = metricRepo.save(metric);
        BillableMetricResponse response = mapper.toResponse(saved);

        response.setProductName(productClient.getProductNameById(saved.getProductId()));
        return response;
    }

    @Override
    public BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request) {
        BillableMetric existing = metricRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));

        validateProductExists(request.getProductId());

        mapper.updateEntityFromDto(request, existing);

        conditionRepo.deleteAll(existing.getUsageConditions());

        List<UsageCondition> updatedConditions = mapper.toUsageConditionEntityList(request.getUsageConditions());
        updatedConditions.forEach(c -> c.setBillableMetric(existing));
        existing.setUsageConditions(updatedConditions);

        BillableMetric saved = metricRepo.save(existing);
        BillableMetricResponse response = mapper.toResponse(saved);

        response.setProductName(productClient.getProductNameById(saved.getProductId()));
        return response;
    }

    @Override
    public BillableMetricResponse getMetricById(Long id) {
        BillableMetric metric = metricRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));

        BillableMetricResponse response = mapper.toResponse(metric);
        response.setProductName(productClient.getProductNameById(metric.getProductId()));
        return response;
    }

    @Override
    public List<BillableMetricResponse> getAllMetrics() {
        List<BillableMetric> all = metricRepo.findAll();
        return all.stream().map(metric -> {
            BillableMetricResponse response = mapper.toResponse(metric);
            response.setProductName(productClient.getProductNameById(metric.getProductId()));
            return response;
        }).toList();
    }

    @Override
    public void deleteMetric(Long id) {
        if (!metricRepo.existsById(id)) {
            throw new ResourceNotFoundException("Metric not found with ID: " + id);
        }
        metricRepo.deleteById(id);
    }

    private void validateProductExists(Long productId) {
        if (!productClient.productExists(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }
    }
}
