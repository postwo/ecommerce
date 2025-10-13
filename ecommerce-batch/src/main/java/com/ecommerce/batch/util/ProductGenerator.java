package com.ecommerce.batch.util;

import com.ecommerce.batch.dto.ProductUploadCsvRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


import java.io.FileWriter;
import java.io.IOException;

public class ProductGenerator { // 다른 클래스에서는 사용 x 여기서 임시로 사용할거기 떄문에 이렇게 적용

    public static void main(String[] args) {
        String csvFilePath = "data/random_product.csv"; // 폴더 경로
        int record = 10_000_000; // 천만건

        //CSVPrinter는 CSV 라이브러리때문에 사용 가능
        try(FileWriter fileWriter = new FileWriter(csvFilePath);
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder()
                    .setHeader(ReflectionUtils.getFiledNames(ProductUploadCsvRow.class).toArray(String[]::new))
                    .build())
        ){

        }catch (IOException e) {
            System.out.println("IO excepiton");
        }
    }
}
