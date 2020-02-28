package com.rbkmoney.proxy.mocketbank.exception;

public class TerminalException extends RuntimeException {

    public TerminalException() {
        super();
    }

    public TerminalException(String message) {
        super(message);
    }

    public TerminalException(Throwable cause) {
        super(cause);
    }

    public TerminalException(String message, Throwable cause) {
        super(message, cause);
    }

}
