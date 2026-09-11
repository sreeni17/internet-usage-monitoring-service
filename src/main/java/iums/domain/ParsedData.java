package iums.domain;

import java.util.List;

public record ParsedData(List<UsageSession> sessions, List<UsageDaily> daily) {
}
