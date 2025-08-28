package com.aforo.billablemetrics.controller;

import com.aforo.billablemetrics.dto.*;
import com.aforo.billablemetrics.service.BillableMetricService;
import lombok.RequiredArgsConstructor;
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
    return ResponseEntity.status(201).body(service.createMetric(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BillableMetricResponse> update(@PathVariable Long id, @RequestBody UpdateBillableMetricRequest req) {
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
}
