package com.example.apigateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TicketServiceProperties.class)
public class LoadBalancerConfig {
    @Bean
    public ServiceInstanceListSupplier ticketServiceInstanceSupplier(TicketServiceProperties properties) {
        return new TicketServiceInstanceSupplier("ticket-service", properties.getInstances());
    }
}
