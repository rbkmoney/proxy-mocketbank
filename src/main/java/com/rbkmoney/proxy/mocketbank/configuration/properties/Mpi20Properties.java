package com.rbkmoney.proxy.mocketbank.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("mpi20")
public class Mpi20Properties {

    private String threeDsServerUrl;
    private String callbackUrl;
    private String threeDsMethodNotificationPath;
    private String acsNotificationPath;
    private String returnUrl;

}
