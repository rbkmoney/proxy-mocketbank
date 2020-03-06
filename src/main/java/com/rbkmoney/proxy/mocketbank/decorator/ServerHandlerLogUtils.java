package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHandlerLogUtils {

    public static void logMessage(Exception ex, String message, Class<?> className) {
        if (ProxyProviderVerification.isUndefinedResultOrUnavailable(ex)) {
            log.warn(className + message, ex);
        } else {
            log.error(className + message, ex);
        }
    }

}
