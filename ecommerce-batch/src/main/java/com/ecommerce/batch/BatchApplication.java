package com.ecommerce.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchApplication { //단순 batch이기때문에 웹 api처럼 서버가 띄워있지않고 바로종료됨
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
