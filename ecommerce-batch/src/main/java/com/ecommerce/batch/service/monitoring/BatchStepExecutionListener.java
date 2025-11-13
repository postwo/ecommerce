package com.ecommerce.batch.service.monitoring;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStepExecutionListener implements StepExecutionListener, ChunkListener {

    private final CustomPrometheusPushGatewayManager manager;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("after step - execution context: {}", stepExecution.getExecutionContext());
        manager.pushMetrics(Map.of("job_name",stepExecution.getJobExecution().getJobInstance().getJobName()));
        return ExitStatus.COMPLETED;
    }

    // 청크가 하나 처리 될떄마다 이벤트를 푸시 매트릭을 통해서 보내줘서 실시간으로 그라파나로 확인
    @Override
    public void afterChunk(ChunkContext context) {
        manager.pushMetrics(Map.of("job_name",context.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName()));
        ChunkListener.super.afterChunk(context);
    }
}