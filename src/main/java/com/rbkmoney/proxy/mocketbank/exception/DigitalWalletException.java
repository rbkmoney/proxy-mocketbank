package com.rbkmoney.proxy.mocketbank.exception;

public class DigitalWalletException extends RuntimeException {

    public DigitalWalletException() {
        super();
    }

    public DigitalWalletException(String message) {
        super(message);
    }

    public DigitalWalletException(Throwable cause) {
        super(cause);
    }

    public DigitalWalletException(String message, Throwable cause) {
        super(message, cause);
    }

}
