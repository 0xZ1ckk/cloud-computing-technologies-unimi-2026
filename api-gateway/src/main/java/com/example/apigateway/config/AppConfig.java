package com.example.apigateway.config;

import com.example.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public JwtUtil jwtUtil(@Value("${app.jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }
}
