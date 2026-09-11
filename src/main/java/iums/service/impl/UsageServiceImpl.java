package iums.service.impl;

import org.springframework.stereotype.Service;
import iums.domain.UsageSession;
import iums.domain.UserTotals;
import iums.exception.InvalidPaginationException;
import iums.exception.InvalidUsernameException;
import iums.exception.InvalidDateException;
import iums.exception.UserNotFoundException;
import iums.repository.UsageRepository;
import iums.resource.AnalyticsResponse;
import iums.resource.UsageBlock;
import iums.resource.UserDetailsDto;
import iums.resource.UserSearchResponse;
import iums.resource.UserUsageDto;
import iums.service.UsageService;
import iums.util.Formats;
import iums.util.Windows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsageServiceImpl implements UsageService {

    private final UsageRepository repository;
    private final Clock clock;

    public UsageServiceImpl(UsageRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public AnalyticsResponse analytics(String date, String pageSize, String limit, String page) {
        LocalDate asOf = Formats.parseAnalyticsDate(date);
        if (asOf.isAfter(LocalDate.now(clock))) {
            throw new InvalidDateException();
        }
        int size = parsePageSize(pageSize, limit);
        int pageNumber = parsePage(page);
        LocalDate from = asOf.minusDays(29);
        int total = repository.countUsers(from, asOf);
        if (total == 0) {
            return AnalyticsResponse.empty();
        }
        int totalPages = (total + size - 1) / size;
        if (pageNumber > totalPages) {
            return AnalyticsResponse.empty();
        }
        int offset = (pageNumber - 1) * size;
        List<UserUsageDto> rows = new ArrayList<>();
        for (UserTotals totals : repository.findTopUsers(asOf, size, offset)) {
            rows.add(new UserUsageDto(
                    totals.username(),
                    Formats.duration(totals.lastDaySeconds()),
                    Formats.duration(totals.last7DaySeconds()),
                    Formats.duration(totals.last30DaySeconds())
            ));
        }
        return AnalyticsResponse.pageOf(rows, size, pageNumber, totalPages);
    }

    @Override
    public UserSearchResponse search(String username, String datetime) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException();
        }
        String exactName = username.trim();
        OffsetDateTime asOf = Formats.parseSearchDatetime(datetime).atOffset(ZoneOffset.UTC);
        if (!repository.userExists(exactName)) {
            throw new UserNotFoundException();
        }
        OffsetDateTime from24h = asOf.minusHours(24);
        List<UsageSession> sessions = repository.findOverlapping(exactName, from24h, asOf);
        return new UserSearchResponse(true, new UserDetailsDto(
                exactName,
                block(sessions, asOf.minusHours(1), asOf),
                block(sessions, asOf.minusHours(6), asOf),
                block(sessions, from24h, asOf)
        ));
    }

    private UsageBlock block(List<UsageSession> sessions, OffsetDateTime from, OffsetDateTime to) {
        long seconds = 0;
        BigDecimal upload = BigDecimal.ZERO;
        BigDecimal download = BigDecimal.ZERO;
        for (UsageSession session : sessions) {
            long overlap = Windows.overlapSeconds(session.startTime(), session.endTime(), from, to);
            if (overlap == 0) {
                continue;
            }
            seconds += overlap;
            upload = upload.add(Windows.share(session.uploadKilobits(), overlap, session.usageSeconds()));
            download = download.add(Windows.share(session.downloadKilobits(), overlap, session.usageSeconds()));
        }
        return new UsageBlock(Formats.duration(seconds), Formats.dataSize(upload), Formats.dataSize(download));
    }

    private int parsePageSize(String pageSize, String limit) {
        String raw = pageSize != null && !pageSize.isBlank() ? pageSize : limit;
        if (raw == null || raw.isBlank()) {
            return 100;
        }
        return parsePositive(raw);
    }

    private int parsePage(String page) {
        if (page == null || page.isBlank()) {
            return 1;
        }
        return parsePositive(page);
    }

    private int parsePositive(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 1) {
                throw new InvalidPaginationException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new InvalidPaginationException();
        }
    }
}
