package iums.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UsageSession(
        String username,
        String macAddress,
        OffsetDateTime startTime,
        int usageSeconds,
        BigDecimal uploadKilobits,
        BigDecimal downloadKilobits,
        OffsetDateTime endTime
) {
}
