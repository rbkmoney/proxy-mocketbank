package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerHandlerLogUtils {
    public static void logMessage(Exception ex, String message) {
        if (ProxyProviderVerification.isUndefinedResultOrUnavailable(ex)) {
            log.warn(message, ex);
        } else {
            log.error(message, ex);
        }
    }
}

