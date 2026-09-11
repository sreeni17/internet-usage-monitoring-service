package iums.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import iums.domain.UsageDaily;
import iums.domain.UsageSession;
import iums.domain.UserTotals;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class UsageRepository {

    private final JdbcTemplate jdbc;

    public UsageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void replaceAll(List<UsageSession> sessions, List<UsageDaily> daily) {
        jdbc.update("TRUNCATE usage_daily, usage_session");
        jdbc.batchUpdate(
                """
                INSERT INTO usage_session
                    (username, mac_address, start_time, usage_seconds, upload_kilobits, download_kilobits, end_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                sessions,
                500,
                (ps, row) -> {
                    ps.setString(1, row.username());
                    ps.setString(2, row.macAddress());
                    ps.setTimestamp(3, Timestamp.from(row.startTime().toInstant()));
                    ps.setInt(4, row.usageSeconds());
                    ps.setBigDecimal(5, row.uploadKilobits());
                    ps.setBigDecimal(6, row.downloadKilobits());
                    ps.setTimestamp(7, Timestamp.from(row.endTime().toInstant()));
                }
        );
        jdbc.batchUpdate(
                """
                INSERT INTO usage_daily
                    (username, usage_date, usage_seconds, upload_kilobits, download_kilobits)
                VALUES (?, ?, ?, ?, ?)
                """,
                daily,
                500,
                (ps, row) -> {
                    ps.setString(1, row.username());
                    ps.setObject(2, row.usageDate());
                    ps.setLong(3, row.usageSeconds());
                    ps.setBigDecimal(4, row.uploadKilobits());
                    ps.setBigDecimal(5, row.downloadKilobits());
                }
        );
    }

    public int countUsers(LocalDate from, LocalDate to) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM (
                    SELECT username
                    FROM usage_daily
                    WHERE usage_date BETWEEN ? AND ?
                    GROUP BY username
                ) ranked
                """,
                Integer.class,
                from,
                to
        );
        return count == null ? 0 : count;
    }

    public List<UserTotals> findTopUsers(LocalDate asOf, int limit, int offset) {
        LocalDate last7From = asOf.minusDays(6);
        LocalDate last30From = asOf.minusDays(29);
        return jdbc.query(
                """
                SELECT username,
                       COALESCE(SUM(usage_seconds) FILTER (WHERE usage_date = ?), 0) AS last_1,
                       COALESCE(SUM(usage_seconds) FILTER (WHERE usage_date BETWEEN ? AND ?), 0) AS last_7,
                       COALESCE(SUM(usage_seconds) FILTER (WHERE usage_date BETWEEN ? AND ?), 0) AS last_30
                FROM usage_daily
                WHERE usage_date BETWEEN ? AND ?
                GROUP BY username
                ORDER BY last_30 DESC, username
                LIMIT ? OFFSET ?
                """,
                (rs, rowNum) -> new UserTotals(
                        rs.getString("username"),
                        rs.getLong("last_1"),
                        rs.getLong("last_7"),
                        rs.getLong("last_30")
                ),
                asOf,
                last7From, asOf,
                last30From, asOf,
                last30From, asOf,
                limit,
                offset
        );
    }

    public boolean userExists(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usage_session WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public List<UsageSession> findOverlapping(String username, OffsetDateTime from, OffsetDateTime to) {
        return jdbc.query(
                """
                SELECT username, mac_address, start_time, usage_seconds, upload_kilobits, download_kilobits, end_time
                FROM usage_session
                WHERE username = ?
                  AND start_time < ?
                  AND end_time > ?
                """,
                (rs, rowNum) -> new UsageSession(
                        rs.getString("username"),
                        rs.getString("mac_address"),
                        rs.getTimestamp("start_time").toInstant().atOffset(ZoneOffset.UTC),
                        rs.getInt("usage_seconds"),
                        rs.getBigDecimal("upload_kilobits"),
                        rs.getBigDecimal("download_kilobits"),
                        rs.getTimestamp("end_time").toInstant().atOffset(ZoneOffset.UTC)
                ),
                username,
                Timestamp.from(to.toInstant()),
                Timestamp.from(from.toInstant())
        );
    }
}
