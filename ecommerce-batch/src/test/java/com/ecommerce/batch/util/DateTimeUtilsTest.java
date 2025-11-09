package com.ecommerce.batch.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsTest {

    @Test
    void testToLocalDate() {
        String date = "2025-11-21";

        LocalDate result = DateTimeUtils.toLocalDate(date);

        assertThat(result).isEqualTo(LocalDate.of(2025, 11, 21));
    }

}
