package com.aforo.billablemetrics.controller;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.service.BillableMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billable-metrics")
@RequiredArgsConstructor
public class BillableMetricController {

    private final BillableMetricService service;

    @PostMapping
    public ResponseEntity<BillableMetricResponse> create(@RequestBody CreateBillableMetricRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMetric(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillableMetricResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateBillableMetricRequest req) {
        return ResponseEntity.ok(service.updateMetric(id, req));
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<BillableMetricResponse> finalizeMetric(@PathVariable Long id) {
        return ResponseEntity.ok(service.finalizeMetric(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillableMetricResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMetricById(id));
    }

    @GetMapping
    public ResponseEntity<List<BillableMetricResponse>> getAll() {
        return ResponseEntity.ok(service.getAllMetrics());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteMetric(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-product")
    public ResponseEntity<List<BillableMetricResponse>> getAll(
            @RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ResponseEntity.ok(service.getMetricsByProductId(productId));
        }
        return ResponseEntity.ok(service.getAllMetrics());
    }

    // INTERNAL: called by Product Service when a product is deleted
    @DeleteMapping("/internal/products/{productId}")
    public ResponseEntity<Void> deleteAllByProduct(@PathVariable Long productId) {
        service.deleteMetricsByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
