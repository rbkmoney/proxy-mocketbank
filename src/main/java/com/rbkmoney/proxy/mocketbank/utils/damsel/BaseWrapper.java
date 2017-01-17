package com.rbkmoney.proxy.mocketbank.utils.damsel;


import com.rbkmoney.damsel.base.Timer;

public class BaseWrapper {

    public static Timer makeTimerTimeout(int timeout) {
        Timer timer = new Timer();
        timer.setTimeout(timeout);
        return timer;
    }

}
