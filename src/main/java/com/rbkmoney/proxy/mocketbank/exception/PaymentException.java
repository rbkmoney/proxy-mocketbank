package com.rbkmoney.proxy.mocketbank.exception;

public class PaymentException extends RuntimeException {

    public PaymentException() {
        super();
    }

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(Throwable cause) {
        super(cause);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
