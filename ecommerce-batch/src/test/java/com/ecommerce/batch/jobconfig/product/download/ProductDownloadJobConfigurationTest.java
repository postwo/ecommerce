package com.ecommerce.batch.jobconfig.product.download;

import com.ecommerce.batch.domain.product.Product;
import com.ecommerce.batch.domain.product.ProductStatus;
import com.ecommerce.batch.jobconfig.BaseBatchIntegrationTest;
import com.ecommerce.batch.service.product.ProductService;
import com.ecommerce.batch.util.DateTimeUtils;
import com.ecommerce.batch.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(properties = {"spring.batch.job.name=productDownloadJob"})
class ProductDownloadJobConfigurationTest extends BaseBatchIntegrationTest {

    @Value("classpath:/data/products_downloaded_expected.csv")
    private Resource expectedResource;
    private File outputFile;
    @Autowired
    private ProductService productService;

    @Test
    public void testJob(@Autowired Job productDownloadJob) throws Exception {
        saveProducts();
        outputFile = FileUtils.createTempFile("products_downloaded", ".csv");
        JobParameters jobParameters = jobParameters();
        jobLauncherTestUtils.setJob(productDownloadJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertAll(() -> assertThat(Files.readString(Path.of(outputFile.getPath()))).isEqualTo(
                        Files.readString(Path.of(expectedResource.getFile().getPath()))),
                () -> assertJobCompleted(jobExecution));
    }

    private void saveProducts() {
        productService.save(
                Product.of("1", 1L, "식품", "햇반", LocalDate.of(2023, 7, 4),
                        LocalDate.of(2026, 5, 28), ProductStatus.AVAILABLE, "아모레퍼시픽1",
                        "나이키코리아1", 25154, 439,
                        DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404"),
                        DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404")));
        productService.save(
                Product.of("2", 2L, "식품", "햇반", LocalDate.of(2023, 7, 4),
                        LocalDate.of(2026, 5, 28), ProductStatus.AVAILABLE, "아모레퍼시픽2",
                        "나이키코리아2", 25154, 439,
                        DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404"),
                        DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404")));
    }


    private JobParameters jobParameters() {
        return new JobParametersBuilder()
                .addJobParameter("outputFilePath",
                        new JobParameter<>(outputFile.getPath(), String.class, false))
                .addJobParameter("gridSize",
                        new JobParameter<>(2, Integer.class, false))
                .toJobParameters();
    }

}