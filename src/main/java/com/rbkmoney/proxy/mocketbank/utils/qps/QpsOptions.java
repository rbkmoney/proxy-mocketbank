package com.rbkmoney.proxy.mocketbank.utils.qps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QpsOptions {

    public static final String QPS_TIMER_TIMEOUT = "qps_timeout";

    public static Integer extractQpsTimerTimeout(Map<String, String> options, int qpsTimeout) {
        return Integer.parseInt(options.getOrDefault(QPS_TIMER_TIMEOUT, String.valueOf(qpsTimeout)));
    }
}
