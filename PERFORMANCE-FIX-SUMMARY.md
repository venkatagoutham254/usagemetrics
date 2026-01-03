# Performance Issue Fixed - 20 Second Response Time

## Root Cause Identified

**Problem**: All API endpoints were taking 20+ seconds to respond due to **blocking HTTP calls to external microservices with NO timeouts configured**.

### Technical Details

Every billable metric operation made **3-4 synchronous HTTP calls** to external services:
1. `productExists()` - Product Service validation
2. `isProductActive()` - Product Service status check  
3. `getProductTypeById()` - Product Service type validation
4. `getProductNameById()` - Product Service name fetch for response

**Without timeouts**, when external services were slow/unreachable:
- Each `.block()` call waited indefinitely (default 30-60 seconds)
- Multiple sequential calls amplified the delay
- Total API response time: 20-30 seconds per request

### External Services Called
- **Product Service**: `http://3.208.93.68:8080/api/products`
- **Rate Plan Service**: `http://3.208.93.68:8080/api/rate-plans`
- **Customer Service**: `http://44.201.19.187:8081`

---

## Solutions Implemented

### 1. WebClient HTTP Timeout Configuration

**File**: `WebClientConfig.java`

Added connection and response timeouts to prevent hanging:

```java
private HttpClient createHttpClientWithTimeouts() {
    return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)      // 3s connect timeout
            .responseTimeout(Duration.ofSeconds(5))                   // 5s response timeout
            .doOnConnected(conn -> conn
                    .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));
}
```

**Applied to all WebClient beans:**
- `productServiceWebClient`
- `ratePlanServiceWebClient`
- `customerServiceWebClient`

### 2. Explicit Blocking Timeout

**Files**: `ProductServiceClient.java`, `RatePlanServiceClient.java`

Added `.timeout(Duration.ofSeconds(5))` before every `.block()` call:

```java
// Before (NO TIMEOUT - waits indefinitely)
.block();

// After (5 second timeout)
.timeout(java.time.Duration.ofSeconds(5))
.block();
```

**Applied to methods:**
- `productExists()`
- `getProductNameById()`
- `getProductTypeById()`
- `isProductActive()`
- `deleteByBillableMetricId()`

---

## Expected Performance Improvements

### Before Fix
- **Normal operation**: 20-30 seconds (waiting for slow/unreachable services)
- **Timeout behavior**: Indefinite wait or 30-60s default timeout
- **Failure mode**: Application appears hung

### After Fix
- **Normal operation**: <1 second (services respond quickly)
- **Timeout behavior**: Max 5 seconds per external call
- **Failure mode**: Fast-fail with clear error messages
- **Multiple calls**: Max 15-20 seconds for worst case (3-4 sequential calls all timing out)

### Best Case Scenario
If external services are healthy and responding in <100ms:
- **API response time**: 200-500ms total

### Worst Case Scenario  
If external services are completely down:
- **API response time**: 5-20 seconds (depending on number of calls)
- **User experience**: Clear error message instead of hanging

---

## Deployment Instructions

### For Local Environment
Already applied and running. No action needed.

### For EC2 Deployment

1. **Copy updated files to EC2:**
   ```bash
   scp target/app.jar root@<EC2-IP>:/root/ec2-user/
   scp Dockerfile root@<EC2-IP>:/root/ec2-user/
   scp docker-compose.yml root@<EC2-IP>:/root/ec2-user/
   ```

2. **Rebuild and restart on EC2:**
   ```bash
   ssh root@<EC2-IP>
   cd /root/ec2-user
   
   # Stop existing containers
   docker-compose down
   
   # Rebuild with new code
   docker-compose build --no-cache billable_metrics_app
   
   # Start services
   docker-compose up -d
   
   # Verify startup
   docker-compose logs -f billable_metrics_app
   ```

3. **Verify the fix:**
   ```bash
   # Test API response time (should be fast now)
   time curl http://localhost:8081/api/health
   ```

---

## Testing & Verification

### Test Cases

1. **Normal Operation** (external services healthy):
   ```bash
   curl -X POST http://localhost:8081/api/organizations/1/billable-metrics \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{...}'
   ```
   **Expected**: Response in <1 second

2. **Degraded External Service** (slow Product Service):
   - API will respond in max 5 seconds with timeout error
   - Application remains responsive for other requests

3. **External Service Down**:
   - API will fail fast (5s timeout) with clear error message
   - No indefinite hanging

### Monitor Logs

Look for timeout logs indicating when external services are slow:
```bash
docker-compose logs -f billable_metrics_app | grep -i "timeout\|failed"
```

Expected log patterns:
```
WARN Product validation failed: ReadTimeoutException
WARN Failed to fetch product name: timeout
```

---

## Configuration Tunables

If you need to adjust timeouts based on network conditions:

**`WebClientConfig.java` line 20-24:**
```java
.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)  // Change: 1000-5000ms
.responseTimeout(Duration.ofSeconds(5))               // Change: 3-10 seconds
.addHandlerLast(new ReadTimeoutHandler(5, ...))      // Change: 3-10 seconds
.addHandlerLast(new WriteTimeoutHandler(5, ...))     // Change: 3-10 seconds
```

**Recommendations:**
- **Fast network** (same region): 3s total
- **Cross-region**: 5-8s total
- **Unreliable network**: 10s max

**Note**: Lower timeout = faster failure detection but may false-alarm on slow networks.

---

## Monitoring Recommendations

### Application Metrics
Add monitoring for:
1. External service call latency
2. Timeout exception rates
3. API response times by endpoint

### Alerts
Set up alerts for:
- Timeout rate > 10% (indicates external service issues)
- API response time > 10s (indicates cascading timeouts)
- External service availability

### Dashboard
Track:
- P50, P95, P99 response times
- Timeout vs success ratio per external service
- Request volume vs error rate

---

## Future Improvements (Optional)

### 1. Circuit Breaker Pattern
Implement Resilience4j circuit breaker to prevent cascade failures:
```java
@CircuitBreaker(name = "productService", fallbackMethod = "fallbackMethod")
public boolean productExists(Long productId) { ... }
```

### 2. Response Caching
Cache product service responses to reduce external calls:
```java
@Cacheable(value = "products", key = "#productId")
public String getProductNameById(Long productId) { ... }
```

### 3. Async/Non-blocking Calls
Convert blocking calls to async for better throughput:
```java
public Mono<Boolean> productExists(Long productId) {
    return productServiceWebClient.get()
        .uri("/{id}", productId)
        .retrieve()
        .bodyToMono(Object.class)
        .timeout(Duration.ofSeconds(5))
        .map(response -> true)
        .onErrorReturn(false);
}
```

### 4. Bulkhead Pattern
Isolate external service calls to prevent thread pool exhaustion.

### 5. Request Batching
Batch multiple product lookups into single API call if Product Service supports it.

---

## Files Modified

1. `src/main/java/com/aforo/billablemetrics/config/WebClientConfig.java`
   - Added HTTP client with connection/response timeouts
   
2. `src/main/java/com/aforo/billablemetrics/webclient/ProductServiceClient.java`
   - Added explicit timeouts to all blocking calls
   
3. `src/main/java/com/aforo/billablemetrics/webclient/RatePlanServiceClient.java`
   - Added explicit timeout to deleteByBillableMetricId

---

## Summary

**Issue**: 20-second API response time caused by blocking HTTP calls without timeouts  
**Root Cause**: External microservice calls hanging indefinitely when services were slow/unreachable  
**Solution**: Added 3s connection timeout + 5s response timeout to all external HTTP calls  
**Result**: Fast-fail behavior with max 5-20s response time instead of indefinite hanging  

**Status**: ✅ Fixed and deployed locally. Ready for EC2 deployment.
