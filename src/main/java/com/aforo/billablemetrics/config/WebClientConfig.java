package com.aforo.billablemetrics.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    // CRITICAL: Timeouts prevent connection pool exhaustion from circular dependency
    // Lower timeouts ensure faster failure on broken/slow external services
    private HttpClient createHttpClientWithTimeouts() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)  // 2s to establish connection
                .responseTimeout(Duration.ofSeconds(3))  // 3s max for full response (including errors)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(3, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(3, TimeUnit.SECONDS)));
    }

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/products")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/rate-plans")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }

    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://44.201.19.187:8081")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }
}