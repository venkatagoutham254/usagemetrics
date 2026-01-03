package com.aforo.billablemetrics.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

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
        long startTime = System.currentTimeMillis();
        log.info("[Rate Plan Service] Deleting rate plans for billable metric - billableMetricId: {}", billableMetricId);
        try {
            ratePlanServiceWebClient.delete()
                    .uri("/internal/billable-metrics/{metricId}", billableMetricId)
                    .header("Authorization", getBearerToken())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Rate Plan Service] Rate plan deletion SUCCESS - billableMetricId: {}, duration: {}ms", billableMetricId, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            // Best-effort: do not block metric deletion if rate plan cleanup fails
            log.warn("[Rate Plan Service] Rate plan cleanup FAILED - billableMetricId: {}, duration: {}ms, error: {}", billableMetricId, duration, e.getMessage());
        }
    }
}
