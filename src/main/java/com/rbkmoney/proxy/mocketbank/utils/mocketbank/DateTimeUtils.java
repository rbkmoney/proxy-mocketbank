package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import java.time.Instant;

public class DateTimeUtils {

    public static String getCurrentDateTimeByPattern(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

}
