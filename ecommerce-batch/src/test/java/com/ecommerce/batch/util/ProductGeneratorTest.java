package com.ecommerce.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductGeneratorTest {

    // 정적인 클래스
    private static class  TestClass{
        private String StringField;
        private int intFiled;
        public static final String CONSTANT = "constant";
    }

    @Test
    @DisplayName("static 상수는 CSV 컬럼에 포함되면 안 된다")
    void testGetFieldNames() {

        List<String> fieldNames = ReflectionUtils.getFiledNames(TestClass.class);

        assertThat(fieldNames).hasSize(2)
                .containsExactly("StringField","intFiled")
                .doesNotContain("CONSTANT");
    }
}