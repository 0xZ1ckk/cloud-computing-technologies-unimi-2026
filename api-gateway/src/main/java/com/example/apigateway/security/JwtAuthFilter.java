package com.example.apigateway.security;

import com.example.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing token");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid token");
        }

        String role = claims.get("role", String.class);
        Object userIdValue = claims.get("userId");
        String userId = userIdValue == null ? null : String.valueOf(userIdValue);
        String username = claims.get("username", String.class);

        if (!isAuthorized(path, exchange.getRequest().getMethod(), role)) {
            return forbidden(exchange, "Insufficient role");
        }

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-User-Id", userId == null ? "" : userId)
                .header("X-User-Role", role == null ? "" : role)
                .header("X-Username", username == null ? "" : username)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isAuthorized(String path, HttpMethod method, String role) {
        if (role == null) {
            return false;
        }

        if (path.startsWith("/tickets")) {
            if (HttpMethod.PUT.equals(method)) {
                return "ADMIN".equals(role);
            }
            return "USER".equals(role) || "ADMIN".equals(role);
        }

        if (path.startsWith("/notifications")) {
            return "ADMIN".equals(role);
        }

        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(body)));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(body)));
    }
}
