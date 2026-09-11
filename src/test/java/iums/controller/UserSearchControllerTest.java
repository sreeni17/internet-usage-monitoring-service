package iums.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import iums.configuration.ControllerAdvice;
import iums.exception.InvalidDatetimeException;
import iums.exception.InvalidUsernameException;
import iums.exception.UserNotFoundException;
import iums.resource.UsageBlock;
import iums.resource.UserDetailsDto;
import iums.resource.UserSearchResponse;
import iums.service.UsageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserSearchController.class)
@Import(ControllerAdvice.class)
class UserSearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsageService usageService;

    @Test
    void returnsUserDetails() throws Exception {
        UsageBlock hour = new UsageBlock("00h33m", "100.5MB", "30.2GB");
        when(usageService.search(any(), any())).thenReturn(new UserSearchResponse(
                true,
                new UserDetailsDto("alice", hour, hour, hour)
        ));
        mvc.perform(get("/user/search").param("username", "alice").param("datetime", "20221104T1543"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.lastHourUsage.time").value("00h33m"));
    }

    @Test
    void returns404WhenUserMissing() throws Exception {
        when(usageService.search(any(), any())).thenThrow(new UserNotFoundException());
        mvc.perform(get("/user/search").param("username", "john").param("datetime", "20221104T1543"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error.message").value("user not found"));
    }

    @Test
    void returns422ForBadDatetime() throws Exception {
        when(usageService.search(any(), any())).thenThrow(new InvalidDatetimeException());
        mvc.perform(get("/user/search").param("username", "alice").param("datetime", "bad"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.message").value("invalid datetime"));
    }

    @Test
    void returns422ForBlankUsername() throws Exception {
        when(usageService.search(any(), any())).thenThrow(new InvalidUsernameException());
        mvc.perform(get("/user/search").param("datetime", "20221104T1543"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.message").value("invalid username"));
    }
}
