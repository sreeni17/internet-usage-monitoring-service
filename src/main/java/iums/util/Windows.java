package iums.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class Windows {

    private Windows() {
    }

    public static long overlapSeconds(OffsetDateTime start, OffsetDateTime end,
                                      OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        OffsetDateTime from = start.isAfter(windowStart) ? start : windowStart;
        OffsetDateTime to = end.isBefore(windowEnd) ? end : windowEnd;
        long seconds = Duration.between(from, to).getSeconds();
        return Math.max(0, seconds);
    }

    public static BigDecimal share(BigDecimal amount, long overlap, long total) {
        if (total <= 0 || overlap <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(overlap))
                .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
    }

    public static List<DaySlice> splitByDay(OffsetDateTime start, long usageSeconds,
                                            BigDecimal upload, BigDecimal download) {
        OffsetDateTime end = start.plusSeconds(usageSeconds);
        List<DaySlice> slices = new ArrayList<>();
        OffsetDateTime cursor = start;
        while (cursor.isBefore(end)) {
            OffsetDateTime nextMidnight = cursor.toLocalDate().plusDays(1)
                    .atStartOfDay()
                    .atOffset(ZoneOffset.UTC);
            OffsetDateTime sliceEnd = nextMidnight.isAfter(end) ? end : nextMidnight;
            long overlap = Duration.between(cursor, sliceEnd).getSeconds();
            if (overlap > 0) {
                slices.add(new DaySlice(
                        cursor.toLocalDate(),
                        overlap,
                        share(upload, overlap, usageSeconds),
                        share(download, overlap, usageSeconds)
                ));
            }
            cursor = sliceEnd;
        }
        return slices;
    }

    public record DaySlice(LocalDate date, long seconds, BigDecimal upload, BigDecimal download) {
    }
}
