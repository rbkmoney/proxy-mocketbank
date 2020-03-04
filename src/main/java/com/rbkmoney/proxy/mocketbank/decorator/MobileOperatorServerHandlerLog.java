package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification;
import com.rbkmoney.mnp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

@Slf4j
@RequiredArgsConstructor
public class MobileOperatorServerHandlerLog implements MnpSrv.Iface {

    private final MnpSrv.Iface handler;

    @Override
    public ResponseData lookup(RequestParams requestParams) throws BadPhoneFormat, OperatorNotFound, TException {
        log.info("Lookup: start with requestParams={}", requestParams);
        try {
            ResponseData responseData = handler.lookup(requestParams);
            log.info("Lookup: finish {} with requestParams={}", responseData, requestParams);
            return responseData;
        } catch (Exception ex) {
            String message = String.format("Failed Lookup with requestParams=%s", requestParams);
            logMessage(ex, message);
            throw ex;
        }
    }

    private void logMessage(Exception ex, String message) {
        if (ProxyProviderVerification.isUndefinedResultOrUnavailable(ex)) {
            log.warn(message, ex);
        } else {
            log.error(message, ex);
        }
    }
}
