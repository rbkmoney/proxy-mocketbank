package com.rbkmoney.proxy.mocketbank.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("timer")
public class TimerProperties {

    @NotNull
    private int redirectTimeout;

    @NotNull
    private int qpsTimeout;

    @NotNull
    private int dwTimeout;

}
