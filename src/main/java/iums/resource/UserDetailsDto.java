package iums.resource;

public record UserDetailsDto(
        String username,
        UsageBlock lastHourUsage,
        UsageBlock last6HourUsage,
        UsageBlock last24HourUsage
) {
}
