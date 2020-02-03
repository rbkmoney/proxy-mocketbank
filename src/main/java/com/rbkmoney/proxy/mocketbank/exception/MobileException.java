package com.rbkmoney.proxy.mocketbank.exception;

public class MobileException extends RuntimeException {

    public MobileException() {
        super();
    }

    public MobileException(String message) {
        super(message);
    }

    public MobileException(Throwable cause) {
        super(cause);
    }

    public MobileException(String message, Throwable cause) {
        super(message, cause);
    }

}
