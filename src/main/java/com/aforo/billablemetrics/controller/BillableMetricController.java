package com.aforo.billablemetrics.controller;

import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.service.BillableMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class BillableMetricController {

    private final BillableMetricService service;

    // Create a new metric
    @PostMapping
    public ResponseEntity<BillableMetricResponse> createMetric(
            @Valid @RequestBody CreateBillableMetricRequest request) {
        return ResponseEntity.ok(service.createMetric(request));
    }

    // Update an existing metric
    @PutMapping("/{id}")
    public ResponseEntity<BillableMetricResponse> updateMetric(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBillableMetricRequest request) {
        return ResponseEntity.ok(service.updateMetric(id, request));
    }

    // Get metric by ID
    @GetMapping("/{id}")
    public ResponseEntity<BillableMetricResponse> getMetricById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMetricById(id));
    }

    // Get all metrics
    @GetMapping
    public ResponseEntity<List<BillableMetricResponse>> getAllMetrics() {
        return ResponseEntity.ok(service.getAllMetrics());
    }

    // Delete a metric by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable Long id) {
        service.deleteMetric(id);
        return ResponseEntity.noContent().build();
    }
}
