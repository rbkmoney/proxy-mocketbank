package com.rbkmoney.proxy.test.utils.hellgate;

import com.rbkmoney.damsel.proxy_provider.ProviderProxyHostSrv;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class HellGateApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(HellGateApi.class);

    @Autowired
    private ProviderProxyHostSrv.Iface providerProxyHostSrv;

    public ByteBuffer processCallback(String tag, ByteBuffer callback) throws TException {
        LOGGER.info("Hellgate: processCallback start");
        ByteBuffer callbackResponse = providerProxyHostSrv.processCallback(tag, callback);
        LOGGER.info("Hellgate: processCallback finish");
        return callbackResponse;
    }

}
