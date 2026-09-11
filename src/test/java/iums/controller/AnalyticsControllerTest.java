package iums.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import iums.configuration.ControllerAdvice;
import iums.exception.InvalidDateException;
import iums.exception.InvalidPaginationException;
import iums.resource.AnalyticsResponse;
import iums.resource.UserUsageDto;
import iums.service.UsageService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import(ControllerAdvice.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsageService usageService;

    @Test
    void returnsPagedUsers() throws Exception {
        when(usageService.analytics(any(), any(), any(), any())).thenReturn(
                AnalyticsResponse.pageOf(
                        List.of(new UserUsageDto("alice", "01h00m", "02h00m", "03h00m")),
                        100, 1, 1
                )
        );
        mvc.perform(get("/analytics").param("date", "24122022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].username").value("alice"))
                .andExpect(jsonPath("$.pageSize").value(100))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void returnsEmptyData() throws Exception {
        when(usageService.analytics(any(), any(), any(), any())).thenReturn(AnalyticsResponse.empty());
        mvc.perform(get("/analytics").param("date", "29122022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void returns422ForInvalidDate() throws Exception {
        when(usageService.analytics(any(), any(), any(), any())).thenThrow(new InvalidDateException());
        mvc.perform(get("/analytics").param("date", "11092026"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error.message").value("invalid date"));
    }

    @Test
    void returns422ForInvalidPagination() throws Exception {
        when(usageService.analytics(any(), any(), any(), any())).thenThrow(new InvalidPaginationException());
        mvc.perform(get("/analytics").param("date", "24122022").param("page", "0"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.message").value("invalid pagination"));
    }
}
