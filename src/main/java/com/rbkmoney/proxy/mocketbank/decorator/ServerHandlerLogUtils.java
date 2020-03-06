package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHandlerLogUtils {

    public static final String MESSAGE_TEMPLATE = "Class {}, message {} ";

    public static void logMessage(Exception ex, String message, Class<?> className) {
        if (ProxyProviderVerification.isUndefinedResultOrUnavailable(ex)) {
            log.warn(MESSAGE_TEMPLATE, className, message, ex);
        } else {
            log.warn(MESSAGE_TEMPLATE, className, message, ex);
        }
    }

}
