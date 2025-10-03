package com.aforo.billablemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${clients.http.connectTimeoutMs:3000}")
    private int connectTimeoutMs;

    @Value("${clients.http.responseTimeoutMs:5000}")
    private long responseTimeoutMs;

    @Value("${clients.http.readTimeoutSec:5}")
    private int readTimeoutSec;

    @Value("${clients.http.writeTimeoutSec:5}")
    private int writeTimeoutSec;

    private WebClient build(WebClient.Builder builder, String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSec))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSec))
                );
        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient productServiceWebClient(
            WebClient.Builder builder,
            @Value("${product.service.url:http://54.238.204.246:8080}") String baseUrl) {
        return build(builder, baseUrl + "/api/products");
    }

    @Bean
    public WebClient ratePlanServiceWebClient(
            WebClient.Builder builder,
            @Value("${product.service.url:http://54.238.204.246:8080}") String baseUrl) {
        // Uses the same Product service host/port; rate plan API is in the same service
        return build(builder, baseUrl + "/api/rateplans");
    }

    @Bean
    public WebClient subscriptionServiceWebClient(
            WebClient.Builder builder,
            @Value("${subscriptions.service.url:http://13.113.70.183:8084}") String baseUrl) {
        return build(builder, baseUrl + "/api/subscriptions");
    }
}

