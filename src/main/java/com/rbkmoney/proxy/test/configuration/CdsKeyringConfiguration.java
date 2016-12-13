package com.rbkmoney.proxy.test.configuration;

import com.rbkmoney.damsel.cds.KeyringSrv;
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
public class CdsKeyringConfiguration {

    @Value("${cds.url.keyring}")
    private Resource resource;

    @Bean
    public KeyringSrv.Iface keyringSrv(ClientEventListener listenerSrv) throws IOException {
        return clientBuilder()
                .withEventListener(listenerSrv)
                .withAddress(resource.getURI())
                .build(KeyringSrv.Iface.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientEventListener listenerSrv() {
        return new CompositeClientEventListener(
                new ClientEventLogListener(),
                new HttpClientEventLogListener()
        );
    }

    @Bean(name = "clientBuilderKeyring")
    public ClientBuilder clientBuilder() {
        return new THSpawnClientBuilder();
    }
}
