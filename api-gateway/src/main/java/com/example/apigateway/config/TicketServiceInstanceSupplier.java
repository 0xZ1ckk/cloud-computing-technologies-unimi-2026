package com.example.apigateway.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

public class TicketServiceInstanceSupplier implements ServiceInstanceListSupplier {
    private final String serviceId;
    private final List<ServiceInstance> instances;

    public TicketServiceInstanceSupplier(String serviceId, List<String> uris) {
        this.serviceId = serviceId;
        this.instances = IntStream.range(0, uris.size())
                .mapToObj(index -> toInstance(serviceId, index, uris.get(index)))
                .toList();
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(instances);
    }

    private ServiceInstance toInstance(String serviceId, int index, String uriValue) {
        URI uri = URI.create(uriValue);
        return new DefaultServiceInstance(serviceId + "-" + index, serviceId, uri.getHost(), uri.getPort(),
                "https".equalsIgnoreCase(uri.getScheme()));
    }
}
