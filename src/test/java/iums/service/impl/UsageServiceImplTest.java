package iums.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import iums.domain.UsageSession;
import iums.domain.UserTotals;
import iums.exception.InvalidDateException;
import iums.exception.InvalidPaginationException;
import iums.exception.InvalidUsernameException;
import iums.exception.UserNotFoundException;
import iums.repository.UsageRepository;
import iums.resource.AnalyticsResponse;
import iums.resource.UserSearchResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageServiceImplTest {

    @Mock
    private UsageRepository repository;

    private UsageServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-11T00:00:00Z"), ZoneOffset.UTC);
        service = new UsageServiceImpl(repository, clock);
    }

    @Test
    void analyticsRejectsFutureDate() {
        assertThatThrownBy(() -> service.analytics("12092026", "10", null, "1"))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    void analyticsReturnsEmptyWhenNoRows() {
        when(repository.countUsers(LocalDate.of(2022, 11, 25), LocalDate.of(2022, 12, 24))).thenReturn(0);
        AnalyticsResponse response = service.analytics("24122022", null, null, null);
        assertThat(response.ok()).isTrue();
        assertThat(response.data()).isEmpty();
        assertThat(response.page()).isNull();
    }

    @Test
    void analyticsPaginatesAndFormatsDurations() {
        when(repository.countUsers(LocalDate.of(2022, 11, 5), LocalDate.of(2022, 12, 4))).thenReturn(2);
        when(repository.findTopUsers(LocalDate.of(2022, 12, 4), 1, 0)).thenReturn(List.of(
                new UserTotals("alice", 3600, 7200, 10800)
        ));
        AnalyticsResponse response = service.analytics("04122022", null, "1", "1");
        assertThat(response.ok()).isTrue();
        assertThat(response.pageSize()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.data().get(0).username()).isEqualTo("alice");
        assertThat(response.data().get(0).lastDayUsage()).isEqualTo("01h00m");
    }

    @Test
    void analyticsReturnsEmptyWhenPageIsPastEnd() {
        when(repository.countUsers(any(), any())).thenReturn(1);
        AnalyticsResponse response = service.analytics("04122022", "10", null, "9");
        assertThat(response.data()).isEmpty();
    }

    @Test
    void analyticsRejectsBadPagination() {
        assertThatThrownBy(() -> service.analytics("04122022", "0", null, "1"))
                .isInstanceOf(InvalidPaginationException.class);
        assertThatThrownBy(() -> service.analytics("04122022", "10", null, "abc"))
                .isInstanceOf(InvalidPaginationException.class);
    }

    @Test
    void searchRejectsBlankUsername() {
        assertThatThrownBy(() -> service.search(" ", "20221104T1543"))
                .isInstanceOf(InvalidUsernameException.class);
    }

    @Test
    void searchReturns404WhenUserMissing() {
        when(repository.userExists("john")).thenReturn(false);
        assertThatThrownBy(() -> service.search("john", "20221104T1543"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void searchProratesSessionsIntoWindows() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 4, 14, 43, 0, 0, ZoneOffset.UTC);
        when(repository.userExists("alice")).thenReturn(true);
        when(repository.findOverlapping(eq("alice"), any(), any())).thenReturn(List.of(
                new UsageSession("alice", "AA:BB:CC:DD:EE:01", start, 3600,
                        new BigDecimal("8000"), new BigDecimal("16000"), start.plusHours(1))
        ));

        UserSearchResponse response = service.search("alice", "20221104T1543");
        assertThat(response.ok()).isTrue();
        assertThat(response.data().username()).isEqualTo("alice");
        assertThat(response.data().lastHourUsage().time()).isEqualTo("01h00m");
        assertThat(response.data().lastHourUsage().upload()).isNotEqualTo("0B");
    }

    @Test
    void searchIgnoresSessionsOutsideAWindow() {
        OffsetDateTime start = OffsetDateTime.of(2022, 11, 3, 16, 0, 0, 0, ZoneOffset.UTC);
        when(repository.userExists("alice")).thenReturn(true);
        when(repository.findOverlapping(eq("alice"), any(), any())).thenReturn(List.of(
                new UsageSession("alice", "AA:BB:CC:DD:EE:01", start, 3600,
                        new BigDecimal("8000"), new BigDecimal("16000"), start.plusHours(1))
        ));
        UserSearchResponse response = service.search("alice", "20221104T1543");
        assertThat(response.data().lastHourUsage().time()).isEqualTo("00h00m");
        assertThat(response.data().last24HourUsage().time()).isEqualTo("01h00m");
    }

    @Test
    void analyticsPrefersPageSizeOverLimit() {
        when(repository.countUsers(any(), any())).thenReturn(1);
        when(repository.findTopUsers(any(), eq(2), eq(0))).thenReturn(List.of(
                new UserTotals("alice", 0, 0, 60)
        ));
        AnalyticsResponse response = service.analytics("04122022", "2", "50", "1");
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.data().get(0).last30DayUsage()).isEqualTo("00h01m");
    }

    @Test
    void searchAllowsKnownUserWithNoOverlap() {
        when(repository.userExists("alice")).thenReturn(true);
        when(repository.findOverlapping(eq("alice"), any(), any())).thenReturn(List.of());
        UserSearchResponse response = service.search("alice", "20221104T1543");
        assertThat(response.data().lastHourUsage().time()).isEqualTo("00h00m");
        assertThat(response.data().lastHourUsage().upload()).isEqualTo("0B");
    }
}
