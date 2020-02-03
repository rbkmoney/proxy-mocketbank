package com.rbkmoney.proxy.mocketbank.exception;

public class WithdrawalException extends RuntimeException {

    public WithdrawalException() {
        super();
    }

    public WithdrawalException(String message) {
        super(message);
    }

    public WithdrawalException(Throwable cause) {
        super(cause);
    }

    public WithdrawalException(String message, Throwable cause) {
        super(message, cause);
    }

}
