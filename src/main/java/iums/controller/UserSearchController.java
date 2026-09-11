package iums.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iums.resource.ErrorResponse;
import iums.resource.UserSearchResponse;
import iums.service.UsageService;

@RestController
@Tag(name = "User Search", description = "Look up internet usage for a single user relative to a timestamp")
public class UserSearchController {

    private final UsageService usageService;

    public UserSearchController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/user/search")
    @Operation(
            summary = "Usage for one user relative to a timestamp",
            description = "Returns last 1 hour, 6 hours, and 24 hours of usage (duration, upload, and download) "
                    + "for the given username as of the provided datetime in UTC."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Last 1 hour, 6 hours and 24 hours of usage for the user",
                    content = @Content(schema = @Schema(implementation = UserSearchResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Invalid username or datetime",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserSearchResponse search(
            @Parameter(description = "Exact username to look up", required = true, example = "brainyHeron5")
            @RequestParam(required = false) String username,
            @Parameter(
                    description = "As-of datetime in YYYYMMDDThhmm (UTC)",
                    required = true,
                    example = "20221104T1543"
            )
            @RequestParam(required = false) String datetime
    ) {
        return usageService.search(username, datetime);
    }
}
