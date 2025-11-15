package com.ecommerce.batch.util;

import com.ecommerce.batch.domain.product.ProductStatus;
import com.ecommerce.batch.dto.upload.ProductUploadCsvRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;

public class ProductGenerator { // 다른 클래스에서는 사용 x 여기서 임시로 사용할거기 떄문에 이렇게 적용

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        String csvFilePath = "data/random_product.csv"; // 폴더 경로
        int recordCount = 10_000_000; // 천만건

        //CSVPrinter는 CSV 라이브러리때문에 사용 가능
        // try 부분은 CSV 파일 생성
        try(FileWriter fileWriter = new FileWriter(csvFilePath);
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder()
                    .setHeader(ReflectionUtils.getFiledNames(ProductUploadCsvRow.class).toArray(String[]::new))
                    .build())
        ){
            for (int i = 0; i < recordCount; i++) {
                printer.printRecord(generateRecord());

                // 천만개생성 되고있는지 모르니까 한번 씩 확인하기 위해 작성
                if (i % 100000 == 0) {
                    System.out.println("Generated " + i + " records");
                }
            }
        }catch (IOException e) {
            System.out.println("IO excepiton");
        }
    }

    private static Object[] generateRecord() {

        //ProductUploadCsvRow 에서 필드들을 뽑아서 가져오기 위한 메서드
        ProductUploadCsvRow productRow = randomProductRow();
        return new Object[]{
                productRow.getSellerId(),
                productRow.getCategory(),
                productRow.getProductName(),
                productRow.getSalesStartDate(),
                productRow.getSalesEndDate(),
                productRow.getProductStatus(),
                productRow.getBrand(),
                productRow.getManufacturer(),
                productRow.getSalesPrice(),
                productRow.getStockQuantity()
        };
    }

    private static ProductUploadCsvRow randomProductRow() {
        String[] CATEGORIES = {"가전", "가구", "패션", "식품", "화장품", "서적", "스포츠", "완구", "음악", "디지털"};
        String[] PRODUCT_NAMES = {"TV", "소파", "셔츠", "햇반", "스킨케어 세트", "소설", "축구공", "레고", "기타", "스마트폰"};
        String[] BRANDS = {"삼성", "LG", "나이키", "아모레퍼시픽", "현대", "BMW", "롯데", "스타벅스", "도미노", "맥도날드"};
        String[] MANUFACTURERS = {"삼성전자", "LG전자", "나이키코리아", "아모레퍼시픽", "현대자동차", "BMW코리아", "롯데제과",
                "스타벅스코리아", "도미노피자", "맥도날드코리아"};
        String[] STATUSES = Arrays.stream(ProductStatus.values()).map(Enum::name)
                .toArray(String[]::new);

        return ProductUploadCsvRow.of(
                randomSellerId(),
                randomChoice(CATEGORIES),
                randomChoice(PRODUCT_NAMES),
                randomDate(2020, 2023),// 랜덤 범위 지정 (2020년 ~ 2023년)
                randomDate(2024, 2026), // 랜덤 범위 지정 (2024년 ~ 2026년)
                randomChoice(STATUSES),
                randomChoice(BRANDS),
                randomChoice(MANUFACTURERS),
                randomSalesPrice(),
                randomStockQuantity()
        );
    }

    private static long randomSellerId() {
        return RANDOM.nextLong(1, 101); // 판매자 100명
    }

    private static int randomSalesPrice() {
        return RANDOM.nextInt(10000, 500001);
    }

    private static int randomStockQuantity() {
        return RANDOM.nextInt(1, 1001);
    }

    private static String randomDate(int startYear, int endYear) {
        int year = RANDOM.nextInt(startYear, endYear + 1);
        int month = RANDOM.nextInt(1, 13); // 1월 에서 12월까지
        int day = RANDOM.nextInt(1, 29); // 1일 에서 28일까지
        return LocalDate.of(year, month, day).toString(); //2025-01-12
    }

    private static String randomChoice(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }
}
