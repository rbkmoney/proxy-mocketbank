package com.rbkmoney.proxy.mocketbank.exception;

public class CardException extends RuntimeException {

    public CardException() {
        super();
    }

    public CardException(String message) {
        super(message);
    }

    public CardException(Throwable cause) {
        super(cause);
    }

    public CardException(String message, Throwable cause) {
        super(message, cause);
    }

}
