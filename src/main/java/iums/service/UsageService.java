package iums.service;

import iums.resource.AnalyticsResponse;
import iums.resource.UserSearchResponse;

public interface UsageService {
    AnalyticsResponse analytics(String date, String pageSize, String limit, String page);

    UserSearchResponse search(String username, String datetime);
}
