package com.ecommerce.batch.jobconfig;


import com.ecommerce.batch.BatchApplication;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

//코드 중복을 제거하고 테스트 코드의 일관성 유지 ,Job 테스트에서 공통으로 사용되는 설정과 유틸리티를 한 곳에 모음
@Sql("/sql/schema.sql") //schema를 가지고 데이터베이스를 미리 만든 다음에 테스트가 끝나면 없어진다
@SpringBatchTest
@SpringJUnitConfig(classes = {BatchApplication.class})
public abstract class BaseBatchIntegrationTest { // 실행 되면 안되기때문에 추상클래스 선언

    // 공통적으로 배치 테스트에서 이거를 쓸수 있을거 같으니까 공용으로 뺀거다
    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 다른 메서드 에서도 자주 사용할 거 같기떄문에 공용으로 뺀다
    protected static void assertJobCompleted(JobExecution jobExecution) {
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}