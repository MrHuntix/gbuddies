package com.gbuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

/**
 * Eureka Discovery Server.
 * Built by netflix, but a part of spring cloud now.
 * Helps micro services find each other.
 * If multiple instances of a micro service is up then each service registers itself with eureka and eureka's job is to ensure that
 * a load balancer has access to each of the registered service
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServer {
    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServer.class, args);
    }
}
