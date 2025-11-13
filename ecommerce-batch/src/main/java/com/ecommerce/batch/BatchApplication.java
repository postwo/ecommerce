package com.ecommerce.batch;

import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

    // 멀티 스레드
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(128); // 기본으로 유지할 스레드 수
        executor.setMaxPoolSize(128); // 생성 가능한 최대 스레드 수
        executor.setQueueCapacity(128); // 작업 큐의 최대 용량 = 스레드 128개가 돌고 있는 상태에서 또 작업 요청이 왔을때 대기할 수 있는 작업의 개수를 의미

        // 기본 스레드를 유의 상태에서 종료할지 여부를 설정
        // = 작업을 다 설정하고 나서 이 128개 중의 일부는 할당이 안 되고 유의 싱테로 남을 수 있기 때문에
        // 그 시점에 타임아웃이 나면 이제 종료 할수 있도록 설정
        executor.setAllowCoreThreadTimeOut(true);

        // 익스큐터 서비스가 종료되도록 명령을 받았을때 하위에 실행되고 있는 스레드들이 task를 다 종료하고 나서
        // 익스큐터를 종료한다는 의미
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10); // 종룡 명령을 받고 이제 기다리는 시간을 10초 설정
        return executor;
    }
}
