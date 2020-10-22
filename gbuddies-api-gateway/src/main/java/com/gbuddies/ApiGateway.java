package com.gbuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Zuul Api Gateway
 * does jwt validation
 * Provides ribbon load balancer on starting by default
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
public class ApiGateway {
    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }
}
