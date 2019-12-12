package com.rbkmoney.proxy.mocketbank.exception;

public class MobileOperatorException extends RuntimeException {

    public MobileOperatorException() {
        super();
    }

    public MobileOperatorException(String message) {
        super(message);
    }

    public MobileOperatorException(Throwable cause) {
        super(cause);
    }

    public MobileOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
