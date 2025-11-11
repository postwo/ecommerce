package com.ecommerce.batch.jobconfig.product.upload;

import com.ecommerce.batch.domain.product.Product;
import com.ecommerce.batch.dto.ProductUploadCsvRow;
import com.ecommerce.batch.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class ProductUploadJobConfiguration {

    //상품 업로드 배치 작업
    @Bean
    public Job productUploadJob(JobRepository jobRepository, JobExecutionListener listener,
                                Step productUploadStep) {
        return new JobBuilder("productUploadJob", jobRepository)
                //RunIdIncrementer는 Job을 실행할 때마다 run.id라는 파라미터를 자동으로 추가하고,
                // 그 값을 1씩 증가시키는 역할을 합니다. 이 run.id는 식별(identifying) 파라미터이므로,
                // 이 기능이 정상적으로 동작한다면 매번 새로운 Job으로 인식
                .incrementer(new RunIdIncrementer())// 이걸 사용하면 매번 새로운 Job Instance로 실행 이전 Job 기록은 그대로 두고, 매번 실행할 때마다 완전히 새로운 Job으로 인식되도록 만드는 방법
                .listener(listener)
                .start(productUploadStep)
                .build();
    }

    @Bean
    public Step productUploadStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  StepExecutionListener stepExecutionListener,
                                  ItemReader<ProductUploadCsvRow> productReader,
                                  ItemProcessor<ProductUploadCsvRow, Product> productProcessor,
                                  ItemWriter<Product> productWriter
                                  ) {
        return new StepBuilder("productUploadStep", jobRepository)
                .<ProductUploadCsvRow, Product>chunk(1000, transactionManager)
                .reader(productReader)
                .processor(productProcessor)
                .writer(productWriter)
                .allowStartIfComplete(true)
                .listener(stepExecutionListener)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<ProductUploadCsvRow> productReader(
            // edit configuration 에서 arg 에 설정 --spirng.boot.job.names=prodcutUploadjob inputFilePath=data/radom_product.csv
            @Value("#{jobParameters['inputFilePath']}") String path
    ){
        return new FlatFileItemReaderBuilder<ProductUploadCsvRow>()
                .name("productReader")
                // 프로젝트 내부 경로를 통해서 파일을 읽어온다
                //FileSystemResource는 절대 경로를 기대하지만, @Value("/data/products_for_upload.csv")로 리소스를 로드하면 상대 경로로 처리될 수 있다
                .resource(new FileSystemResource(path)) // 읽을 파일
                .delimited()//컴마로 나눠진 걸 읽는다
                .names(ReflectionUtils.getFiledNames(ProductUploadCsvRow.class).toArray(String[]::new)) // 콤마로 파싱한 다음에 읽어지는 그 값들을 매핑 해준다
                .targetType(ProductUploadCsvRow.class)
                .linesToSkip(1) // 첫째줄이 header이기 때문에 첫째줄은 넘어가게 한다
                .build();
    }

    @Bean
    public ItemProcessor<ProductUploadCsvRow, Product> productProcessor(){
        return Product::from; // 정적 생성자 하나 만들어서 사용
    }

    // db에 저장
    @Bean
    public JdbcBatchItemWriter<Product> productWriter(DataSource dataSource){
        String sql =
                "insert into products( product_id, seller_id, category, product_name, sales_start_date, sales_end_date,"
                        + "product_status, brand, manufacturer, sales_price, stock_quantity) "
                        + "VALUES (:productId, :sellerId, :category, :productName, :salesStartDate, :salesEndDate, "
                        + ":productStatus, :brand, :manufacturer, :salesPrice, :stockQuantity) ";
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }
}
