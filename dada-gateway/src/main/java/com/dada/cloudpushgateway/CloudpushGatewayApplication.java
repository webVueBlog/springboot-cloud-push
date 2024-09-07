package com.dada.cloudpushgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudpushGatewayApplication {

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                //basic proxy
//                .route(r -> r.path("/baidu/ss")
//                        .uri("http://10.9.212.119:8001/server/get")
//                ).build();
//    }

    public static void main(String[] args) {
        SpringApplication.run(CloudpushGatewayApplication.class, args);
    }

}
