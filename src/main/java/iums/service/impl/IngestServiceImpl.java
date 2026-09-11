package iums.service.impl;

import org.springframework.stereotype.Service;
import iums.domain.ParsedData;
import iums.domain.UsageDaily;
import iums.domain.UsageSession;
import iums.exception.IngestException;
import iums.repository.UsageRepository;
import iums.service.IngestService;
import iums.util.Formats;
import iums.util.Windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IngestServiceImpl implements IngestService {

    private final UsageRepository repository;

    public IngestServiceImpl(UsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void ingest(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new IngestException("dataset file not found: " + path.toAbsolutePath());
        }
        ParsedData parsed;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            parsed = parse(reader);
        } catch (IOException ex) {
            throw new IngestException("failed to read dataset", ex);
        }
        repository.replaceAll(parsed.sessions(), parsed.daily());
        System.out.printf("Ingested %d sessions and %d daily rows%n",
                parsed.sessions().size(), parsed.daily().size());
    }

    @Override
    public ParsedData parse(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (header == null || !header.toLowerCase().contains("username")) {
            throw new IngestException("invalid csv header");
        }

        List<UsageSession> sessions = new ArrayList<>();
        Map<String, Acc> daily = new HashMap<>();
        String line;
        int lineNo = 1;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            if (line.isBlank()) {
                continue;
            }
            try {
                UsageSession session = parseRow(line);
                sessions.add(session);
                for (Windows.DaySlice slice : Windows.splitByDay(
                        session.startTime(), session.usageSeconds(),
                        session.uploadKilobits(), session.downloadKilobits())) {
                    String key = session.username() + "|" + slice.date();
                    daily.computeIfAbsent(key, ignored -> new Acc(session.username(), slice.date()))
                            .add(slice);
                }
            } catch (RuntimeException ex) {
                throw new IngestException("invalid row at line " + lineNo + ": " + ex.getMessage(), ex);
            }
        }

        List<UsageDaily> dailyRows = new ArrayList<>();
        for (Acc acc : daily.values()) {
            dailyRows.add(acc.toRow());
        }
        return new ParsedData(sessions, dailyRows);
    }

    private UsageSession parseRow(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("expected 6 columns");
        }
        String username = parts[0].trim();
        String mac = parts[1].trim();
        OffsetDateTime start = Formats.parseSessionStart(parts[2]).atOffset(ZoneOffset.UTC);
        int seconds = Formats.parseUsageTime(parts[3]);
        BigDecimal upload = new BigDecimal(parts[4].trim());
        BigDecimal download = new BigDecimal(parts[5].trim());
        return new UsageSession(username, mac, start, seconds, upload, download, start.plusSeconds(seconds));
    }

    private static final class Acc {
        private final String username;
        private final LocalDate date;
        private long seconds;
        private BigDecimal upload = BigDecimal.ZERO;
        private BigDecimal download = BigDecimal.ZERO;

        private Acc(String username, LocalDate date) {
            this.username = username;
            this.date = date;
        }

        private void add(Windows.DaySlice slice) {
            seconds += slice.seconds();
            upload = upload.add(slice.upload());
            download = download.add(slice.download());
        }

        private UsageDaily toRow() {
            return new UsageDaily(username, date, seconds, upload, download);
        }
    }
}
