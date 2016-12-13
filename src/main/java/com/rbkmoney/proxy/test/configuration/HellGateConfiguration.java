package com.rbkmoney.proxy.test.configuration;

import com.rbkmoney.damsel.proxy_provider.ProviderProxyHostSrv;
import com.rbkmoney.woody.api.ClientBuilder;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.event.CompositeClientEventListener;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.HttpClientEventLogListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class HellGateConfiguration {

    @Value("${hellgate.url}")
    private Resource resource;

    @Bean
    public ProviderProxyHostSrv.Iface providerProxyHost(ClientEventListener listenerSrv) throws IOException {
        return clientBuilder()
                .withEventListener(listenerSrv)
                .withAddress(resource.getURI())
                .build(ProviderProxyHostSrv.Iface.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientEventListener listenerSrv() {
        return new CompositeClientEventListener(
                new ClientEventLogListener(),
                new HttpClientEventLogListener()
        );
    }

    @Bean(name = "clientBuilderHellgate")
    public ClientBuilder clientBuilder() {
        return new THSpawnClientBuilder();
    }

}
