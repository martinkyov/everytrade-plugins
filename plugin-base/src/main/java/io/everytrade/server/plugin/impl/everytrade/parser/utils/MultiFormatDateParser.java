package io.everytrade.server.plugin.impl.everytrade.parser.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

/**
 * Parses the DATE column of the WhaleBooks-family CSV exports, which is written in a number of
 * interchangeable layouts (dot / slash / dash separated, day-first or year-first, with or without
 * time and seconds). Values without a zone are interpreted as UTC.
 *
 * <p>Shared by the WhaleBooks parser bean and the Srajtofle one so both accept exactly the same
 * set of date layouts.
 */
public final class MultiFormatDateParser {

    /*
    d: Day of month (1-31), allows single or double digits.
    M: Month of year (1-12), allows single or double digits.
    H: Hour of day (0-23), allows single or double digits.
    m: Minute of hour (0-59), allows single or double digits.
    s: Second of minute (0-59), allows single or double digits.
     */
    private static final List<String> DATE_PATTERNS = Arrays.asList(
        // Dates with time and seconds
        "d.M.yyyy H:m:s",
        "d/M/yyyy H:m:s",
        "d-M-yyyy H:m:s",
        "yyyy.M.d H:m:s",
        "yyyy-M-d H:m:s",
        "yyyy/M/d H:m:s",
        // Dates with time without seconds
        "d.M.yyyy H:m",
        "d/M/yyyy H:m",
        "d-M-yyyy H:m",
        "yyyy.M.d H:m",
        "yyyy-M-d H:m",
        "yyyy/M/d H:m",
        // Dates without time
        "d.M.yyyy",
        "d/M/yyyy",
        "d-M-yyyy",
        "yyyy.M.d",
        "yyyy-M-d",
        "yyyy/M/d"
    );

    private static final List<DateTimeFormatter> DATE_FORMATTERS = DATE_PATTERNS.stream()
        .map(pattern -> DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC))
        .toList();

    private MultiFormatDateParser() {
    }

    /**
     * @throws IllegalArgumentException when the value matches none of the supported layouts
     */
    public static Instant parse(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                TemporalAccessor temporal = formatter.parseBest(
                    value,
                    Instant::from,
                    LocalDateTime::from,
                    LocalDate::from
                );
                if (temporal instanceof Instant) {
                    return (Instant) temporal;
                } else if (temporal instanceof LocalDateTime) {
                    return ((LocalDateTime) temporal).toInstant(ZoneOffset.UTC);
                } else if (temporal instanceof LocalDate) {
                    return ((LocalDate) temporal).atStartOfDay(ZoneOffset.UTC).toInstant();
                }
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + value);
    }
}
