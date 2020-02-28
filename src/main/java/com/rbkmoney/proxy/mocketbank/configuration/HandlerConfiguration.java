package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.decorator.*;
import com.rbkmoney.proxy.mocketbank.handler.mobile.MobileServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.oct.OctServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.p2p.P2pServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.terminal.TerminalServerHandler;
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
    public MobileOperatorServerHandlerLog mobileOperatorServerHandlerLog(MobileOperatorServerHandler mobileOperatorServerHandler) {
        return new MobileOperatorServerHandlerLog(mobileOperatorServerHandler);
    }

    @Bean
    @Primary
    public MobileServerHandlerLog mobileServerHandlerLog(MobileServerHandler mobileServerHandler) {
        return new MobileServerHandlerLog(mobileServerHandler);
    }

    @Bean
    @Primary
    public P2pServerHandlerLog p2pServerHandlerLog(P2pServerHandler p2pServerHandler) {
        return new P2pServerHandlerLog(p2pServerHandler);
    }

    @Bean
    @Primary
    public TerminalServerHandlerLog terminalServerHandlerLog(TerminalServerHandler terminalServerHandler) {
        return new TerminalServerHandlerLog(terminalServerHandler);
    }

}
