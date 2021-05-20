package com.rbkmoney.proxy.mocketbank.exception;

public class RedirectTemplateException extends RuntimeException {

    public RedirectTemplateException() {
        super();
    }

    public RedirectTemplateException(String message) {
        super(message);
    }

    public RedirectTemplateException(Throwable cause) {
        super(cause);
    }

    public RedirectTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
