package iums.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import iums.domain.ParsedData;
import iums.domain.UsageDaily;
import iums.domain.UsageSession;
import iums.exception.IngestException;
import iums.repository.UsageRepository;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IngestServiceImplTest {

    @Mock
    private UsageRepository repository;

    @InjectMocks
    private IngestServiceImpl ingestService;

    @Test
    void parseBuildsSessionsAndDailyRows() throws Exception {
        String csv = """
                username, mac_address, start_time, usage_time, upload, download
                alice,AA:BB:CC:DD:EE:01,2022-11-04 10:00:00,1:00:00,8000,16000
                bob,AA:BB:CC:DD:EE:02,2022-11-04 23:00:00,2:00:00,16000,8000
                """;
        ParsedData parsed = ingestService.parse(new BufferedReader(new StringReader(csv)));

        assertThat(parsed.sessions()).hasSize(2);
        assertThat(parsed.sessions().get(0).username()).isEqualTo("alice");
        assertThat(parsed.sessions().get(0).usageSeconds()).isEqualTo(3600);
        assertThat(parsed.daily()).hasSize(3);

        UsageDaily bobNov4 = parsed.daily().stream()
                .filter(row -> row.username().equals("bob") && row.usageDate().equals(LocalDate.of(2022, 11, 4)))
                .findFirst()
                .orElseThrow();
        assertThat(bobNov4.usageSeconds()).isEqualTo(3600);
        assertThat(bobNov4.uploadKilobits()).isEqualByComparingTo("8000");
    }

    @Test
    void parseSkipsBlankLinesAndRejectsEmptyFile() throws Exception {
        assertThatThrownBy(() -> ingestService.parse(new BufferedReader(new StringReader(""))))
                .isInstanceOf(IngestException.class)
                .hasMessageContaining("header");

        String csv = """
                username, mac_address, start_time, usage_time, upload, download

                alice,AA:BB:CC:DD:EE:01,2022-11-04 10:00:00,1:00:00,8000,16000
                """;
        ParsedData parsed = ingestService.parse(new BufferedReader(new StringReader(csv)));
        assertThat(parsed.sessions()).hasSize(1);
    }

    @Test
    void parseRejectsBadHeader() {
        assertThatThrownBy(() -> ingestService.parse(new BufferedReader(new StringReader("foo,bar\n"))))
                .isInstanceOf(IngestException.class)
                .hasMessageContaining("header");
    }

    @Test
    void parseRejectsBadRow() {
        String csv = """
                username, mac_address, start_time, usage_time, upload, download
                alice,only-two-columns
                """;
        assertThatThrownBy(() -> ingestService.parse(new BufferedReader(new StringReader(csv))))
                .isInstanceOf(IngestException.class)
                .hasMessageContaining("line 2");
    }

    @Test
    void ingestWritesParsedRows() throws Exception {
        Path file = Files.createTempFile("sessions", ".csv");
        Files.writeString(file, """
                username, mac_address, start_time, usage_time, upload, download
                alice,AA:BB:CC:DD:EE:01,2022-11-04 10:00:00,1:00:00,8000,16000
                """);
        ingestService.ingest(file.toString());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UsageSession>> sessions = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UsageDaily>> daily = ArgumentCaptor.forClass(List.class);
        verify(repository).replaceAll(sessions.capture(), daily.capture());
        assertThat(sessions.getValue()).hasSize(1);
        assertThat(daily.getValue()).hasSize(1);
        assertThat(daily.getValue().get(0).uploadKilobits()).isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void ingestFailsWhenFileMissing() {
        assertThatThrownBy(() -> ingestService.ingest("missing.csv"))
                .isInstanceOf(IngestException.class)
                .hasMessageContaining("not found");
    }
}
