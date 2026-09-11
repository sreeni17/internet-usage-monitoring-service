package iums.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnalyticsResponse(
        boolean ok,
        List<UserUsageDto> data,
        Integer pageSize,
        Integer page,
        Integer totalPages
) {
    public static AnalyticsResponse empty() {
        return new AnalyticsResponse(true, List.of(), null, null, null);
    }

    public static AnalyticsResponse pageOf(List<UserUsageDto> data, int pageSize, int page, int totalPages) {
        return new AnalyticsResponse(true, data, pageSize, page, totalPages);
    }
}
