package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.*;

import java.util.List;

public interface BillableMetricService {
    BillableMetricResponse createMetric(CreateBillableMetricRequest request);
    BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request);
    BillableMetricResponse finalizeMetric(Long id);         // <<--
    BillableMetricResponse getMetricById(Long id);
    List<BillableMetricResponse> getAllMetrics();
    void deleteMetric(Long id);

    List<BillableMetricResponse> getMetricsByProductId(Long productId);

    // Deletes all billable metrics for the given productId for the current tenant
    void deleteMetricsByProductId(Long productId);

  }
