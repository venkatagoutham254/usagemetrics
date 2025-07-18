package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;

import java.util.List;

public interface BillableMetricService {

    BillableMetricResponse createMetric(CreateBillableMetricRequest request);

    BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request);

    BillableMetricResponse getMetricById(Long id);

    List<BillableMetricResponse> getAllMetrics();

    void deleteMetric(Long id);
}
