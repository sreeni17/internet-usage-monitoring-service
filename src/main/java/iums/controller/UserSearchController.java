package iums.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iums.resource.UserSearchResponse;
import iums.service.UsageService;

@RestController
public class UserSearchController {

    private final UsageService usageService;

    public UserSearchController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/user/search")
    public UserSearchResponse search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String datetime
    ) {
        return usageService.search(username, datetime);
    }
}
