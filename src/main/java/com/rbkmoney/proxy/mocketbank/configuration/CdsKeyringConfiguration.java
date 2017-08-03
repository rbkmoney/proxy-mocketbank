package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.damsel.cds.KeyringSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class CdsKeyringConfiguration {

    @Value("${cds.url.keyring}")
    private Resource resource;

    @Bean
    public KeyringSrv.Iface keyringSrv() throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .build(KeyringSrv.Iface.class);
    }

}
