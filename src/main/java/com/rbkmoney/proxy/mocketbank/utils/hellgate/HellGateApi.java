package com.rbkmoney.proxy.mocketbank.utils.hellgate;

import com.rbkmoney.damsel.proxy_provider.ProviderProxyHostSrv;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class HellGateApi {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProviderProxyHostSrv.Iface providerProxyHostSrv;

    public ByteBuffer processPaymentCallback(String tag, ByteBuffer callback) throws TException {
        LOGGER.info("Hellgate: processCallback start");
        ByteBuffer callbackResponse = providerProxyHostSrv.processPaymentCallback(tag, callback);
        LOGGER.info("Hellgate: processCallback finish");
        return callbackResponse;
    }


    public ByteBuffer processRecurrentTokenCallback(String tag, ByteBuffer callback) throws TException {
        LOGGER.info("Hellgate: processCallback start");
        ByteBuffer callbackResponse = providerProxyHostSrv.processRecurrentTokenCallback(tag, callback);
        LOGGER.info("Hellgate: processCallback finish");
        return callbackResponse;
    }

}
