package com.rbkmoney.proxy.mocketbank.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("adapter-mock-bank")
public class AdapterMockBankProperties {

    @NotEmpty
    private String callbackUrl;

    @NotEmpty
    private String pathCallbackUrl;

    @NotEmpty
    private String pathRecurrentCallbackUrl;

}
