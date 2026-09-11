package iums.util;

import org.junit.jupiter.api.Test;
import iums.exception.InvalidDateException;
import iums.exception.InvalidDatetimeException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatsTest {

    @Test
    void parsesUnpaddedUsageTime() {
        assertThat(Formats.parseUsageTime("4:50:20")).isEqualTo(4 * 3600 + 50 * 60 + 20);
        assertThat(Formats.parseUsageTime("23:59:59")).isEqualTo(86399);
    }

    @Test
    void rejectsBadUsageTime() {
        assertThatThrownBy(() -> Formats.parseUsageTime("4:50")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Formats.parseUsageTime("4:99:00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Formats.parseUsageTime("-1:00:00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Formats.parseUsageTime("1:-1:00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Formats.parseUsageTime("1:00:99")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parsesSessionStart() {
        assertThat(Formats.parseSessionStart("2022-11-04 15:43:33"))
                .isEqualTo(LocalDateTime.of(2022, 11, 4, 15, 43, 33));
    }

    @Test
    void parsesAnalyticsDate() {
        assertThat(Formats.parseAnalyticsDate("24122022")).isEqualTo(LocalDate.of(2022, 12, 24));
    }

    @Test
    void rejectsInvalidAnalyticsDate() {
        assertThatThrownBy(() -> Formats.parseAnalyticsDate(null)).isInstanceOf(InvalidDateException.class);
        assertThatThrownBy(() -> Formats.parseAnalyticsDate("")).isInstanceOf(InvalidDateException.class);
        assertThatThrownBy(() -> Formats.parseAnalyticsDate("32132022")).isInstanceOf(InvalidDateException.class);
        assertThatThrownBy(() -> Formats.parseAnalyticsDate("20221224")).isInstanceOf(InvalidDateException.class);
    }

    @Test
    void parsesSearchDatetime() {
        assertThat(Formats.parseSearchDatetime("20221104T1543"))
                .isEqualTo(LocalDateTime.of(2022, 11, 4, 15, 43));
    }

    @Test
    void rejectsInvalidSearchDatetime() {
        assertThatThrownBy(() -> Formats.parseSearchDatetime(" ")).isInstanceOf(InvalidDatetimeException.class);
        assertThatThrownBy(() -> Formats.parseSearchDatetime("2022-11-04")).isInstanceOf(InvalidDatetimeException.class);
    }

    @Test
    void formatsDuration() {
        assertThat(Formats.duration(33 * 60)).isEqualTo("00h33m");
        assertThat(Formats.duration(12 * 3600 + 33 * 60)).isEqualTo("12h33m");
        assertThat(Formats.duration(330 * 3600 + 8 * 60)).isEqualTo("330h08m");
        assertThat(Formats.duration(-5)).isEqualTo("00h00m");
    }

    @Test
    void formatsDataSize() {
        assertThat(Formats.dataSize(BigDecimal.ZERO)).isEqualTo("0B");
        assertThat(Formats.dataSize(null)).isEqualTo("0B");
        assertThat(Formats.dataSize(new BigDecimal("-1"))).isEqualTo("0B");
        assertThat(Formats.dataSize(new BigDecimal("8"))).isEqualTo("1000.0B");
        assertThat(Formats.dataSize(new BigDecimal("8192"))).isEqualTo("1000.0KB");
    }
}
