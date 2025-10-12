package com.ecommerce.batch.domain.product;

public enum ProductStatus {
    AVAILABLE("판매 중"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("판매 종료");

    final String desc; // 내용

    ProductStatus(String desc) {
        this.desc = desc;
    }
}