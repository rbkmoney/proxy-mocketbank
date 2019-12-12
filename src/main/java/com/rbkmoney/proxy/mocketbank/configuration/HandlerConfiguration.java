package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.decorator.MobileOperatorServerHandlerLog;
import com.rbkmoney.proxy.mocketbank.decorator.WithdrawalServerHandlerLog;
import com.rbkmoney.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.oct.OctServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HandlerConfiguration {

    @Bean
    @Primary
    public WithdrawalServerHandlerLog withdrawalServerHandlerLog(OctServerHandler octServerHandler) {
        return new WithdrawalServerHandlerLog(octServerHandler);
    }

    @Bean
    @Primary
    public MobileOperatorServerHandlerLog mobileServerHandlerLog(MobileOperatorServerHandler mobileServerHandler) {
        return new MobileOperatorServerHandlerLog(mobileServerHandler);
    }

}
