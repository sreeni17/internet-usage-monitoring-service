package iums.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindowsTest {

    @Test
    void overlapIsZeroWhenOutsideWindow() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 4, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(1);
        OffsetDateTime winStart = OffsetDateTime.of(2022, 11, 4, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime winEnd = winStart.plusHours(1);
        assertThat(Windows.overlapSeconds(start, end, winStart, winEnd)).isZero();
    }

    @Test
    void overlapClipsToWindow() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 4, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(2);
        OffsetDateTime winStart = OffsetDateTime.of(2022, 11, 4, 11, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime winEnd = OffsetDateTime.of(2022, 11, 4, 12, 0, 0, 0, ZoneOffset.UTC);
        assertThat(Windows.overlapSeconds(start, end, winStart, winEnd)).isEqualTo(3600);
    }

    @Test
    void shareIsZeroWhenNoOverlap() {
        assertThat(Windows.share(new BigDecimal("100"), 0, 60)).isEqualByComparingTo("0");
        assertThat(Windows.share(new BigDecimal("100"), 10, 0)).isEqualByComparingTo("0");
    }

    @Test
    void splitsSessionAcrossMidnight() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 2, 23, 25, 34, 0, ZoneOffset.UTC);
        BigDecimal upload = new BigDecimal("9680905.67");
        BigDecimal download = new BigDecimal("3332745.31");
        List<Windows.DaySlice> slices = Windows.splitByDay(start, 35622, upload, download);

        assertThat(slices).hasSize(2);
        assertThat(slices.get(0).date()).isEqualTo(LocalDate.of(2022, 11, 2));
        assertThat(slices.get(0).seconds()).isEqualTo(2066);
        assertThat(slices.get(1).date()).isEqualTo(LocalDate.of(2022, 11, 3));
        assertThat(slices.get(1).seconds()).isEqualTo(33556);
        assertThat(slices.get(0).upload().add(slices.get(1).upload()).subtract(upload).abs())
                .isLessThan(new BigDecimal("0.01"));
        assertThat(slices.get(0).download().add(slices.get(1).download()).subtract(download).abs())
                .isLessThan(new BigDecimal("0.01"));
    }

    @Test
    void keepsSameDaySessionOnOneDate() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 4, 10, 0, 0, 0, ZoneOffset.UTC);
        List<Windows.DaySlice> slices = Windows.splitByDay(start, 3600, BigDecimal.TEN, BigDecimal.ONE);
        assertThat(slices).hasSize(1);
        assertThat(slices.get(0).date()).isEqualTo(LocalDate.of(2022, 11, 4));
        assertThat(slices.get(0).seconds()).isEqualTo(3600);
        assertThat(slices.get(0).upload()).isEqualByComparingTo("10");
    }
}
