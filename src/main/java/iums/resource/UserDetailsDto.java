package iums.resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Per-window usage for one user")
public record UserDetailsDto(
        @Schema(description = "Exact username that was looked up", example = "brainyHeron5")
        String username,
        @Schema(description = "Usage in the hour ending at the as-of datetime")
        UsageBlock lastHourUsage,
        @Schema(description = "Usage in the 6 hours ending at the as-of datetime")
        UsageBlock last6HourUsage,
        @Schema(description = "Usage in the 24 hours ending at the as-of datetime")
        UsageBlock last24HourUsage
) {
}
