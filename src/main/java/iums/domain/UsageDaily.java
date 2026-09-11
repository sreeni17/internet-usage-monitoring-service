package iums.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UsageDaily(
        String username,
        LocalDate usageDate,
        long usageSeconds,
        BigDecimal uploadKilobits,
        BigDecimal downloadKilobits
) {
}
