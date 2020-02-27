package com.rbkmoney.proxy.mocketbank.exception;

public class UnsupportedMethodException extends RuntimeException {

    public UnsupportedMethodException() {
        super();
    }

    public UnsupportedMethodException(String message) {
        super(message);
    }

    public UnsupportedMethodException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMethodException(String message, Throwable cause) {
        super(message, cause);
    }

}
