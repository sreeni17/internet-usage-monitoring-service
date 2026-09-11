package iums.resource;

public record UserUsageDto(
        String username,
        String lastDayUsage,
        String last7DayUsage,
        String last30DayUsage
) {
}
