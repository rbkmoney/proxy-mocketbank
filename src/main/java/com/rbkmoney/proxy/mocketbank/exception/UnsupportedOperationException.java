package com.rbkmoney.proxy.mocketbank.exception;

public class UnsupportedOperationException extends RuntimeException {

    public UnsupportedOperationException() {
        super();
    }

    public UnsupportedOperationException(String message) {
        super(message);
    }

    public UnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
