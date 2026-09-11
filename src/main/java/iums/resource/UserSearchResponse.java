package iums.resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful user search payload")
public record UserSearchResponse(
        @Schema(description = "Whether the request succeeded", example = "true")
        boolean ok,
        @Schema(description = "Usage details for the requested user")
        UserDetailsDto data
) {
}
