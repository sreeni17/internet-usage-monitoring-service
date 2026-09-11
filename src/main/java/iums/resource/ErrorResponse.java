package iums.resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error payload returned for 4xx responses")
public record ErrorResponse(
        @Schema(description = "Always false for errors", example = "false")
        boolean ok,
        @Schema(description = "Error details")
        ErrorBody error
) {

    public record ErrorBody(
            @Schema(description = "Human-readable error message", example = "user not found")
            String message
    ) {
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, new ErrorBody(message));
    }
}
