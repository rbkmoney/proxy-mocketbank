package com.rbkmoney.proxy.mocketbank.utils.damsel;


import com.rbkmoney.damsel.base.*;
import com.rbkmoney.damsel.base.Error;

import java.nio.ByteBuffer;

public class BaseWrapper {

    public static Content makeContent(String type, byte[] data) {
        Content content = new Content();
        content.setType(type);
        content.setData(data);
        return content;
    }

    public static Content makeContent(String type, ByteBuffer data) {
        Content content = new Content();
        content.setType(type);
        content.setData(data);
        return content;
    }

    public static Rational makeRational(long p, long q) {
        Rational rational = new Rational();
        rational.setP(p);
        rational.setQ(q);
        return rational;
    }

    public static Error makeError(String code, String description) {
        Error error = new Error();
        error.setCode(code);
        error.setDescription(description);
        return error;
    }

    public static Error makeError(String code) {
        return BaseWrapper.makeError(code, null);
    }

    public static Timer makeTimerDeadLine(String deadLine) {
        Timer timer = new Timer();
        timer.setDeadline(deadLine);
        return timer;
    }

    public static Timer makeTimerTimeout(int timeout) {
        Timer timer = new Timer();
        timer.setTimeout(timeout);
        return timer;
    }

    public static Ok makeOk() {
        return new Ok();
    }

}
