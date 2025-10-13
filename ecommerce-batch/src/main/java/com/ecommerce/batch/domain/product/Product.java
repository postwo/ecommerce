package com.ecommerce.batch.domain.product;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 빈 생성자를 외부에서 편하게 쓰기위해 프로덕트를 상속받은곳에서만 쓸수있게 지정
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 전체 필드에 대한 생성자도 프라이빗으로 선언해서 샹송자는 정적 생성자만 쓸수 있게 세팅
public class Product { // 상품

    private String productId; //productId는 csv파일에 넣어주는게 아니다
    private Long sellerId;// 판매자 아이디


    private String category;
    private String productName; //상품명
    private LocalDate salesStartDate; // 판매 시작일
    private LocalDate salesEndDate; // 판매 종료일
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus; // 상품의 상태
    private String brand;
    private String manufacturer; // 제조사

    private int salesPrice; // 상품의 가격
    private int stockQuantity; // 상품의 수량
    private LocalDateTime createdAt; // 데이터 생성된 시간
    private LocalDateTime updatedAt; // 데이터 수정된 시간


}
