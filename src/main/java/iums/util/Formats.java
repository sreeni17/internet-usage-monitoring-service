package iums.util;

import iums.exception.InvalidDateException;
import iums.exception.InvalidDatetimeException;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class Formats {

    private static final DateTimeFormatter SESSION_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter ANALYTICS_DATE =
            DateTimeFormatter.ofPattern("ddMMuuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter SEARCH_DATETIME =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmm").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    private Formats() {
    }

    public static int parseUsageTime(String value) {
        String[] parts = value.trim().split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid usage_time: " + value);
        }
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        if (hours < 0 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException("invalid usage_time: " + value);
        }
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static LocalDateTime parseSessionStart(String value) {
        return LocalDateTime.parse(value.trim(), SESSION_TIME);
    }

    public static LocalDate parseAnalyticsDate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDateException();
        }
        try {
            return LocalDate.parse(value.trim(), ANALYTICS_DATE);
        } catch (DateTimeException ex) {
            throw new InvalidDateException();
        }
    }

    public static LocalDateTime parseSearchDatetime(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDatetimeException();
        }
        try {
            return LocalDateTime.parse(value.trim(), SEARCH_DATETIME);
        } catch (DateTimeException ex) {
            throw new InvalidDatetimeException();
        }
    }

    public static String duration(long seconds) {
        long safe = Math.max(0, seconds);
        long hours = safe / 3600;
        long minutes = (safe % 3600) / 60;
        return String.format("%02dh%02dm", hours, minutes);
    }

    public static String dataSize(BigDecimal kilobits) {
        if (kilobits == null || kilobits.signum() <= 0) {
            return "0B";
        }
        double bytes = kilobits.doubleValue() * 1000.0 / 8.0;
        int unit = 0;
        while (bytes >= 1024 && unit < SIZE_UNITS.length - 1) {
            bytes /= 1024;
            unit++;
        }
        return String.format("%.1f%s", bytes, SIZE_UNITS[unit]);
    }
}
