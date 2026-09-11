package iums.resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Duration and data transferred in a time window")
public record UsageBlock(
        @Schema(description = "Session overlap duration as HHhMMm", example = "00h33m")
        String time,
        @Schema(description = "Upload volume in this window", example = "100.5MB")
        String upload,
        @Schema(description = "Download volume in this window", example = "30.2GB")
        String download
) {
}
