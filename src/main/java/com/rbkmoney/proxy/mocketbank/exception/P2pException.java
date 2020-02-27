package com.rbkmoney.proxy.mocketbank.exception;

public class P2pException extends RuntimeException {

    public P2pException() {
        super();
    }

    public P2pException(String message) {
        super(message);
    }

    public P2pException(Throwable cause) {
        super(cause);
    }

    public P2pException(String message, Throwable cause) {
        super(message, cause);
    }

}
