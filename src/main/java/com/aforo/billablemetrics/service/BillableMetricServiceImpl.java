package com.aforo.billablemetrics.service;

import com.aforo.billablemetrics.dto.BillableMetricResponse;
import com.aforo.billablemetrics.dto.CreateBillableMetricRequest;
import com.aforo.billablemetrics.dto.UpdateBillableMetricRequest;
import com.aforo.billablemetrics.entity.BillableMetric;
import com.aforo.billablemetrics.entity.TransactionFormat;
import com.aforo.billablemetrics.exception.ResourceNotFoundException;
import com.aforo.billablemetrics.mapper.BillableMetricMapper;
import com.aforo.billablemetrics.repository.BillableMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillableMetricServiceImpl implements BillableMetricService {

    private final BillableMetricRepository repository;
    private final BillableMetricMapper mapper;

    @Override
    public BillableMetricResponse createMetric(CreateBillableMetricRequest request) {
        BillableMetric metric = mapper.toEntity(request);
        nullifyUnrelatedFields(metric);
        return mapper.toResponse(repository.save(metric));
    }

    @Override
    public BillableMetricResponse updateMetric(Long id, UpdateBillableMetricRequest request) {
        BillableMetric existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        mapper.updateEntityFromDto(request, existing);
        nullifyUnrelatedFields(existing);
        return mapper.toResponse(repository.save(existing));
    }

    @Override
    public BillableMetricResponse getMetricById(Long id) {
        BillableMetric metric = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with ID: " + id));
        return mapper.toResponse(metric);
    }

    @Override
    public List<BillableMetricResponse> getAllMetrics() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMetric(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Metric not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // ⛔ Nullify unrelated fields based on format
    private void nullifyUnrelatedFields(BillableMetric metric) {
        TransactionFormat format = metric.getTransactionFormat();

        if (format == null) return;

        switch (format) {
            case API -> {
                // ✅ Keep API fields
                nullLLM(metric);
                nullDataExchange(metric);
                nullEmbeddedAnalytics(metric);
                nullAiAgent(metric);
            }
            case LLM -> {
                nullApi(metric);
                // ✅ Keep LLM fields
                nullDataExchange(metric);
                nullEmbeddedAnalytics(metric);
                nullAiAgent(metric);
            }
            case DATA_EXCHANGE -> {
                nullApi(metric);
                nullLLM(metric);
                // ✅ Keep Data Exchange
                nullEmbeddedAnalytics(metric);
                nullAiAgent(metric);
            }

            case EMBEDDED_ANALYTICS -> {
                nullApi(metric);
                nullLLM(metric);
                nullDataExchange(metric);
                // ✅ Keep EmbeddedAnalytics
                nullAiAgent(metric);
            }
            case AI_AGENT -> {
                nullApi(metric);
                nullLLM(metric);
                nullDataExchange(metric);
                nullEmbeddedAnalytics(metric);
                // ✅ Keep AI Agent
            }
        }
    }

    private void nullApi(BillableMetric m) {
        m.setApiName(null);
        m.setApiVersion(null);
        m.setApiPath(null);
        m.setApiMethod(null);
    }

    private void nullLLM(BillableMetric m) {
        m.setLlmModel(null);
        m.setLlmVersion(null);
        m.setLlmEndpointType(null);
    }

    private void nullDataExchange(BillableMetric m) {
        m.setJobType(null);
        m.setSourceSystem(null);
        m.setTargetSystem(null);
    }

    private void nullEmbeddedAnalytics(BillableMetric m) {
        m.setDashboardId(null);
        m.setWidgetId(null);
        m.setAppSection(null);
        m.setInteractionType(null);
        m.setAnalyticsUserRole(null);
    }

    private void nullAiAgent(BillableMetric m) {
        m.setAgentId(null);
        m.setAgentVersion(null);
        m.setDeployment(null);
        m.setTriggerType(null);
        m.setChannel(null);
        m.setAgentRole(null);
    }
}
