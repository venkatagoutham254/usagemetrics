package com.aforo.billablemetrics.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.aforo.billablemetrics.tenant.TenantContext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class RatePlanServiceClient {

    private final WebClient ratePlanServiceWebClient;

    private String getBearerToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return "Bearer " + jwt.getTokenValue();
        }
        return null;
    }

    // Called when a billable metric is deleted to cascade cleanup in Rate Plan Service
    public void deleteByBillableMetricId(Long billableMetricId) {
        try {
            ratePlanServiceWebClient.delete()
                    .uri("/internal/billable-metrics/{metricId}", billableMetricId)
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            // Best-effort: do not block metric deletion if rate plan cleanup fails
            log.warn("RatePlan cleanup failed for metric {}: {}", billableMetricId, e.getMessage());
        }
    }

    // Returns true if there's at least one ACTIVE rate plan for the given product that is linked to the metric
    public boolean hasActiveRatePlanForMetric(Long productId, Long billableMetricId) {
        try {
            RatePlanSummary[] plans = ratePlanServiceWebClient.get()
                    .uri("/product/{productId}", productId)
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(RatePlanSummary[].class)
                    .block();
            if (plans == null) return false;
            for (RatePlanSummary p : plans) {
                if (p == null) continue;
                String status = p.getStatus() == null ? null : p.getStatus().trim().toUpperCase();
                if ("ACTIVE".equals(status)) {
                    if (billableMetricId == null) return true; // any active plan counts
                    Long linked = p.getBillableMetricId();
                    if (linked != null && linked.equals(billableMetricId)) return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check active rate plans for product {} / metric {}: {}", productId, billableMetricId, e.getMessage());
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RatePlanSummary {
        private Long billableMetricId;
        private String status;
        public Long getBillableMetricId() { return billableMetricId; }
        public void setBillableMetricId(Long id) { this.billableMetricId = id; }
        public String getStatus() { return status; }
        public void setStatus(String s) { this.status = s; }
    }
}

