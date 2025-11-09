package com.ecommerce.batch.jobconfig.product.upload;

import com.ecommerce.batch.jobconfig.BaseBatchIntegrationTest;
import com.ecommerce.batch.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(properties = {"spring.batch.job.name= productUploadJob"}) // 잡이 여러개있으면 여기서 설정후 테스트 하면 된다
class ProductUploadJobConfigurationTest extends BaseBatchIntegrationTest {

    @Value("classpath:/data/products_for_upload.csv")
    private Resource input;

    @Autowired
    private ProductService productService;

    // configuration 에다가 정의할 bean을 @Autowired Job productUploadJob 여기서 주입받는다
    @Test
    @DisplayName("CSV 파일에서 제품 데이터를 읽어서 데이터베이스에 업로드하는 전체 배치 프로세스가 정상 작동하는지 검증")
    public void testJob(@Autowired Job productUploadJob) throws Exception {
        // given
        JobParameters jobParameters = jobParameters();
        jobLauncherTestUtils.setJob(productUploadJob);

        // when
        // Job이 정상적으로 실행되는가?
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
            // productService.countProducts()).isEqualTo(6) = 올바른 개수의 데이터가 저장되었는가
        // assertJobCompleted(jobExecution) = Job의 최종 상태가 COMPLETED인가
        assertAll(() -> assertThat(productService.countProducts()).isEqualTo(6) ,
                () -> assertJobCompleted(jobExecution));
    }

    //Job 실행에 필요한 파라미터를 생성하는 헬퍼 메서드
    //배치 Job에게 "어떤 파일을 읽어야 하는지" 알려주는 것
    private JobParameters
    jobParameters() throws IOException {

        // "inputFilePath",  // 파라미터 이름
        //  input.getFile().getPath(),  // 값: CSV 파일의 실제 경로
        // String.class,               // 타입: 문자열
        // false                       // identifying: Job 식별에 사용 안함
        return new JobParametersBuilder()
                .addJobParameter("inputFilePath",
                        //input.getFile().getPath()로 절대 경로를 전달
                        new JobParameter<>(input.getFile().getPath(), String.class, false))
                .toJobParameters();
    }

}
