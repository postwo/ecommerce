package com.ecommerce.batch.dto;

import lombok.*;

@Data
@Setter
@NoArgsConstructor // 접근제한을 안건이유는 dto는 객체를 만들고 외부에서 필드를 넣을 수 있기 떄문에 안건거다
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductUploadCsvRow {

    private Long sellerId;// 판매자 아이디

    private String category;
    private String productName; //상품명
    private String salesStartDate; // 판매 시작일
    private String salesEndDate; // 판매 종료일
    private String productStatus; // 상품의 상태, csv로우 파일을 읽었을때 enum으로 구별하지 않고 String을 사용
    private String brand;
    private String manufacturer; // 제조사

    private int salesPrice; // 상품의 가격
    private int stockQuantity; // 상품의 수량
}
