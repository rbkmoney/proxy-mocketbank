package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.damsel.proxy_provider.ProviderProxyHostSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class HellGateConfiguration {

    @Value("${hellgate.url}")
    private Resource resource;

    @Bean
    public ProviderProxyHostSrv.Iface providerProxyHost() throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .build(ProviderProxyHostSrv.Iface.class);
    }

}
