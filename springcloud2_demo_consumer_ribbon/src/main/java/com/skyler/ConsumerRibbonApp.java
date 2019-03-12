package com.skyler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

/**
 * Description:
 * <p></p>
 * <pre>
 *
 *   NB.
 * </pre>
 * <p>
 * Created by skyler on 2019-02-26 at 18:20
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ConsumerRibbonApp {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerRibbonApp.class, args);
    }
}
