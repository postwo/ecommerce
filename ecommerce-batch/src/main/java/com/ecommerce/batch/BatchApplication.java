package com.ecommerce.batch;

import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BatchApplication { //단순 batch이기때문에 웹 api처럼 서버가 띄워있지않고 바로종료됨
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    public PushGateway pushGateway(
            @Value("${prometheus.pushgateway.url:localhost:9091}") String url) {
        return new PushGateway(url);
    }
}
