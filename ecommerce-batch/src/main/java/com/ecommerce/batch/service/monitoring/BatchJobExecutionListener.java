package com.ecommerce.batch.service.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobExecutionListener implements JobExecutionListener {// job 실행 시작이랑 졸료에 대한부분만 가져다 사용

    private final CustomPrometheusPushGatewayManager manager;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("listener: before Job");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("listener: after Job {}", jobExecution.getExecutionContext());
        manager.pushMetrics(Map.of("job_name",jobExecution.getJobInstance().getJobName()));
    }


}
