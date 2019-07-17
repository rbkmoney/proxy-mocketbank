package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-d'T'HH:mm:ss";

    public static String getCurrentDateTimeByPattern(String timestamp) {
        return getCurrentDateTimeByPattern(timestamp, DATE_TIME_PATTERN);
    }

    public static String getCurrentDateTimeByPattern(String timestamp, String pattern) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(timestamp)), ZoneOffset.UTC);
        return zdt.format(DateTimeFormatter.ofPattern(pattern));
    }

}
