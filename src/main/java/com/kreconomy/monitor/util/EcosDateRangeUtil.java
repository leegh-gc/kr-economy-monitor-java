package com.kreconomy.monitor.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EcosDateRangeUtil {

    public record DateRange(String start, String end) {}

    /**
     * ECOS API 기간 코드(A/Q/M/D)에 따라 날짜 범위를 계산한다.
     */
    public static DateRange buildRange(String period) {
        LocalDate today = LocalDate.now();
        return switch (period.toUpperCase()) {
            case "A" -> {
                // 연간: 최근 10년
                String start = String.valueOf(today.getYear() - 10);
                String end = String.valueOf(today.getYear());
                yield new DateRange(start, end);
            }
            case "Q" -> {
                // 분기: 최근 10년
                int startYear = today.getYear() - 10;
                int quarter = (today.getMonthValue() - 1) / 3 + 1;
                String start = startYear + "Q1";
                String end = today.getYear() + "Q" + quarter;
                yield new DateRange(start, end);
            }
            case "M" -> {
                // 월간: 최근 10년
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMM");
                String start = today.minusYears(10).format(fmt);
                String end = today.format(fmt);
                yield new DateRange(start, end);
            }
            case "D" -> {
                // 일간: 최근 1년
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
                String start = today.minusYears(1).format(fmt);
                String end = today.format(fmt);
                yield new DateRange(start, end);
            }
            default -> throw new IllegalArgumentException("Unknown period: " + period);
        };
    }
}
