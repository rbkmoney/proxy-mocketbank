package com.rbkmoney.proxy.mocketbank.configuration.properties;

import com.rbkmoney.adapter.common.properties.CommonAdapterProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("adapter-mock-mpi")
public class AdapterMockMpiProperties extends CommonAdapterProperties {

}
