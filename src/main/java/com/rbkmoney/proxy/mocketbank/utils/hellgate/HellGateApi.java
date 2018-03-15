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

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ProviderProxyHostSrv.Iface providerProxyHostSrv;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link HellGateApi HellGateApi} instance.
     */
    public HellGateApi() {
        // Constructs default a new {@link HellGateApi HellGateApi} instance.
    }

    /**
     * Constructs a new {@link HellGateApi HellGateApi} instance with the given
     * initial parameters to be constructed.
     *
     * @param providerProxyHostSrv the field's storageSrv (see {@link #providerProxyHostSrv}).
     */
    @Autowired
    public HellGateApi(ProviderProxyHostSrv.Iface providerProxyHostSrv) {
        this.providerProxyHostSrv = providerProxyHostSrv;
    }

    public ByteBuffer processPaymentCallback(String tag, ByteBuffer callback) throws TException {
        log.info("processPaymentCallback start with tag {}", tag);
        try {
            ByteBuffer callbackResponse = providerProxyHostSrv.processPaymentCallback(tag, callback);
            log.info("processPaymentCallback finish with tag {}", tag);
            return callbackResponse;
        } catch (Exception ex) {
            throw new HellGateException(String.format("Exception in processPaymentCallback with tag: %s", tag), ex);
        }
    }

    public ByteBuffer processRecurrentTokenCallback(String tag, ByteBuffer callback) throws TException {
        log.info("processRecurrentTokenCallback start with tag {}", tag);
        try {
            ByteBuffer callbackResponse = providerProxyHostSrv.processRecurrentTokenCallback(tag, callback);
            log.info("processRecurrentTokenCallback finish with tag {}", tag);
            return callbackResponse;
        } catch (Exception ex) {
            throw new HellGateException(String.format("Exception in processRecurrentTokenCallback with tag: %s", tag), ex);
        }
    }

}
