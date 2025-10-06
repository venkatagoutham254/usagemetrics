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
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("internal", "billable-metrics")
                            .path("/{metricId}")
                            .build(billableMetricId))
                    .headers(h -> {
                        String token = getBearerToken();
                        if (token != null) h.set("Authorization", token);
                        try { h.set("X-Organization-Id", String.valueOf(TenantContext.require())); } catch (Exception ignored) {}
                    })
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            // Best-effort: do not block metric deletion if rate plan cleanup fails
            log.warn("RatePlan cleanup failed for metric {}: {}", billableMetricId, e.getMessage());
        }
    }

    // Returns true if there's at least one CONFIGURED/LIVE rate plan for the given product linked to the metric
    public boolean hasActiveRatePlanForMetric(Long productId, Long billableMetricId) {
        Long orgId = null;
        try { orgId = TenantContext.require(); } catch (Exception ignored) {}
        return hasActiveRatePlanForMetric(productId, billableMetricId, orgId);
    }

    // Overload with explicit orgId for reliable cross-service tenant propagation
    public boolean hasActiveRatePlanForMetric(Long productId, Long billableMetricId, Long orgId) {
        try {
            // 1) Fast path: dedicated internal linkage check
            try {
                Boolean linked = ratePlanServiceWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .pathSegment("internal", "active-link")
                                .queryParam("productId", productId)
                                .queryParam("billableMetricId", billableMetricId)
                                .build())
                        .headers(h -> {
                            String token = getBearerToken();
                            if (token != null) h.set("Authorization", token);
                            if (orgId != null) h.set("X-Organization-Id", String.valueOf(orgId));
                        })
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();
                if (Boolean.TRUE.equals(linked)) return true;
            } catch (Exception ignored) {
                // proceed to list-based fallbacks
            }

            // 2) Fallback: product-scoped list
            RatePlanSummary[] plans = ratePlanServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("product")
                            .path("/{productId}")
                            .build(productId))
                    .headers(h -> {
                        String token = getBearerToken();
                        if (token != null) h.set("Authorization", token);
                        if (orgId != null) h.set("X-Organization-Id", String.valueOf(orgId));
                    })
                    .retrieve()
                    .bodyToMono(RatePlanSummary[].class)
                    .block();
            // 3) Fallback: if product-scoped list is null/empty OR doesn't contain a matching metric linkage,
            // query all plans and filter by metric id
            boolean matchedInProductScope = false;
            if (plans != null) {
                for (RatePlanSummary p : plans) {
                    if (p == null) continue;
                    String status = p.getStatus() == null ? null : p.getStatus().trim().toUpperCase();
                    if ("CONFIGURED".equals(status) || "LIVE".equals(status)) {
                        if (billableMetricId == null) { matchedInProductScope = true; break; }
                        Long linked = p.getBillableMetricId();
                        if (linked != null && linked.equals(billableMetricId)) { matchedInProductScope = true; break; }
                    }
                }
            }

            if (!matchedInProductScope) {
                plans = ratePlanServiceWebClient.get()
                        .uri(uriBuilder -> uriBuilder.build())
                        .headers(h -> {
                            String token = getBearerToken();
                            if (token != null) h.set("Authorization", token);
                            if (orgId != null) h.set("X-Organization-Id", String.valueOf(orgId));
                        })
                        .retrieve()
                        .bodyToMono(RatePlanSummary[].class)
                        .block();
            }
            if (plans == null) return false;
            for (RatePlanSummary p : plans) {
                if (p == null) continue;
                String status = p.getStatus() == null ? null : p.getStatus().trim().toUpperCase();
                if ("CONFIGURED".equals(status) || "LIVE".equals(status)) {
                    if (billableMetricId == null) return true;
                    Long linked = p.getBillableMetricId();
                    if (linked != null && linked.equals(billableMetricId)) return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check active rate plans for product {} / metric {} (orgId={}): {}", productId, billableMetricId, orgId, e.getMessage());
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

