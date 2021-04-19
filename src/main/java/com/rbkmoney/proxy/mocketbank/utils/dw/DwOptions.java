package com.rbkmoney.proxy.mocketbank.utils.dw;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DwOptions {

    public static final String DW_TIMER_TIMEOUT = "dw_timeout";

    public static Integer extractDigitalWalletTimerTimeout(Map<String, String> options, int timeout) {
        return Integer.parseInt(options.getOrDefault(DW_TIMER_TIMEOUT, String.valueOf(timeout)));
    }
}
