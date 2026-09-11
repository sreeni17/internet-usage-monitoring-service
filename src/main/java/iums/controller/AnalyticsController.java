package iums.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iums.resource.AnalyticsResponse;
import iums.service.UsageService;

@RestController
public class AnalyticsController {

    private final UsageService usageService;

    public AnalyticsController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/analytics")
    public AnalyticsResponse analytics(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String pageSize,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String page
    ) {
        return usageService.analytics(date, pageSize, limit, page);
    }
}
