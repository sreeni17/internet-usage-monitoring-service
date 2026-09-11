package iums.domain;

public record UserTotals(String username, long lastDaySeconds, long last7DaySeconds, long last30DaySeconds) {
}
