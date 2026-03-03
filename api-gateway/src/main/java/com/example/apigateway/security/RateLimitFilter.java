package com.example.apigateway.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final long refillSeconds;

    public RateLimitFilter(
            @Value("${app.rate-limit.capacity}") int capacity,
            @Value("${app.rate-limit.refill-seconds}") long refillSeconds
    ) {
        this.capacity = capacity;
        this.refillSeconds = refillSeconds;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/tickets") || !HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId == null || userId.isBlank()) {
            return chain.filter(exchange);
        }

        Bucket bucket = buckets.computeIfAbsent(userId, key -> Bucket4j.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofSeconds(refillSeconds))))
                .build());

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        byte[] body = "Rate limit exceeded".getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
