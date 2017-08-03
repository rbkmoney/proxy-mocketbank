package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.damsel.cds.StorageSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class CdsStorageConfiguration {

    @Value("${cds.url.storage}")
    private Resource resource;

    @Bean
    public StorageSrv.Iface storageSrv() throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .build(StorageSrv.Iface.class);
    }

}
