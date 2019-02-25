package com.skyler.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description: 此处的配置与在application.yml中的配置相同
 * <p></p>
 * <pre>
 *  application.yml file
 *
 *  spring:
 *   cloud:
 *     gateway:
 *      routes:
 *        - id: service1
 *          uri: lb://service1
 *          predicates:
 *            - Path=/api-a/**
 * </pre>
 * Created by skyler on 2019-02-25 at 20:41
 */
@Configuration
public class RouteMappingConfiguration {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes().route(r -> r.path("/api-a/**").uri("lb://service1")).build();
    }
}
