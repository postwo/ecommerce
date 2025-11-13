package com.ecommerce.batch.jobconfig.product.upload;

import com.ecommerce.batch.domain.product.Product;
import com.ecommerce.batch.dto.ProductUploadCsvRow;
import com.ecommerce.batch.service.file.SplitFilePartitioner;
import com.ecommerce.batch.util.FileUtils;
import com.ecommerce.batch.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;

@Slf4j
@Configuration
public class ProductUploadJobConfiguration {

    //상품 업로드 배치 작업
    @Bean
    public Job productUploadJob(JobRepository jobRepository, JobExecutionListener listener,
                                Step productUploadPartitionStep) {
        return new JobBuilder("productUploadJob", jobRepository)
                //RunIdIncrementer는 Job을 실행할 때마다 run.id라는 파라미터를 자동으로 추가하고,
                // 그 값을 1씩 증가시키는 역할을 합니다. 이 run.id는 식별(identifying) 파라미터이므로,
                // 이 기능이 정상적으로 동작한다면 매번 새로운 Job으로 인식
//                .incrementer(new RunIdIncrementer())// 이걸 사용하면 매번 새로운 Job Instance로 실행 이전 Job 기록은 그대로 두고, 매번 실행할 때마다 완전히 새로운 Job으로 인식되도록 만드는 방법
                .listener(listener)
                .start(productUploadPartitionStep)
                .build();
    }

    //productUploadPartitionStep.getName() 이렇게 주입받아서 넣어도 되고
    // "productUploadPartitionStep" 이렇게 문자열로 감싸서 넣어줘도 된다
    @Bean
    public Step productUploadPartitionStep(JobRepository jobRepository
            , Step productUploadPartitionStep, SplitFilePartitioner splitFilePartitioner
            , PartitionHandler filePartitionHandler){
        return new StepBuilder("productUploadPartitionStep",jobRepository)
                .partitioner(productUploadPartitionStep.getName(),splitFilePartitioner)//쪼개져서 실행될 스텝명
                .partitionHandler(filePartitionHandler)
                .allowStartIfComplete(true) // 완료가 되어도 재실행될수 있게
                .build();
    }

    @Bean
    @JobScope //jobParameters를 사용 할려면 이걸 걸어줘야 한다
    public SplitFilePartitioner splitFilePartitioner(
            @Value("#{jobParameters['inputFilePath']}") String path,
            @Value("#{jobParameters['gridSize']}") int gridSize
    ){
        return new SplitFilePartitioner(FileUtils.splitCsv(new File(path),gridSize));
    }

    @Bean
    @JobScope //jobParameters를 사용 할려면 이걸 걸어줘야 한다
    public TaskExecutorPartitionHandler filePartitionHandler(TaskExecutor taskExecutor,
                                                             Step productUploadStep, @Value("#{jobParameters['gridSize']}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(taskExecutor);
        handler.setStep(productUploadStep);
        handler.setGridSize(gridSize);
        return handler;
    }

    @Bean
    public Step productUploadStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  StepExecutionListener stepExecutionListener,
                                  ItemReader<ProductUploadCsvRow> productReader,
                                  ItemProcessor<ProductUploadCsvRow, Product> productProcessor,
                                  ItemWriter<Product> productWriter,
                                  TaskExecutor taskExecutor
                                  ) {
        return new StepBuilder("productUploadStep", jobRepository)
                .<ProductUploadCsvRow, Product>chunk(1000, transactionManager)
                .reader(productReader)
                .processor(productProcessor)
                .writer(productWriter)
                .allowStartIfComplete(true)
                .listener(stepExecutionListener)
                .taskExecutor(taskExecutor) //청크 단위로 스레드가 병리로 돌아가기 때문에 chunk를 사용하는 스텝부분에 테스크 익스큐터를 주입
                .build();
    }

    // 멀티 쓰레드 적용하면서 동시서 이슈 발생 그래서 SynchronizedItemStreamReader를 적용
    // 쪼개진 파일들이 들어온다
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<ProductUploadCsvRow> productReader(
            // edit configuration 에서 arg 에 설정 --spirng.boot.job.names=prodcutUploadjob inputFilePath=data/radom_product.csv
            @Value("#{stepExecutionContext['file']}") File file
    ){
        FlatFileItemReader<ProductUploadCsvRow> fileItemReader  = new FlatFileItemReaderBuilder<ProductUploadCsvRow>()
                .name("productReader")
                // 프로젝트 내부 경로를 통해서 파일을 읽어온다
                //FileSystemResource는 절대 경로를 기대하지만, @Value("/data/products_for_upload.csv")로 리소스를 로드하면 상대 경로로 처리될 수 있다
                .resource(new FileSystemResource(file)) // 읽을 파일
                .delimited()//컴마로 나눠진 걸 읽는다
                .names(ReflectionUtils.getFiledNames(ProductUploadCsvRow.class).toArray(String[]::new)) // 콤마로 파싱한 다음에 읽어지는 그 값들을 매핑 해준다
                .targetType(ProductUploadCsvRow.class)
                .linesToSkip(1) // 첫째줄이 header이기 때문에 첫째줄은 넘어가게 한다
                .build();
        return new SynchronizedItemStreamReaderBuilder<ProductUploadCsvRow>()
                .delegate(fileItemReader)
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
