package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.decorator.*;
import com.rbkmoney.proxy.mocketbank.handler.digital.wallet.DigitalWalletServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.mobile.MobileServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.oct.OctServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.payment.PaymentServerHandler;
import com.rbkmoney.proxy.mocketbank.handler.terminal.TerminalServerHandler;
import org.springframework.context.annotation.*;

@Configuration
public class HandlerConfiguration {

    @Bean
    @Primary
    public WithdrawalServerHandlerLog withdrawalServerHandlerLog(OctServerHandler octServerHandler) {
        return new WithdrawalServerHandlerLog(octServerHandler);
    }

    @Bean
    public MobileOperatorServerHandlerLog mobileOperatorServerHandlerLog(
            MobileOperatorServerHandler mobileOperatorServerHandler) {
        return new MobileOperatorServerHandlerLog(mobileOperatorServerHandler);
    }

    @Bean
    public MobileServerHandlerLog mobileServerHandlerLog(MobileServerHandler mobileServerHandler) {
        return new MobileServerHandlerLog(mobileServerHandler);
    }

    @Bean
    public TerminalServerHandlerLog terminalServerHandlerLog(TerminalServerHandler terminalServerHandler) {
        return new TerminalServerHandlerLog(terminalServerHandler);
    }

    @Bean
    public DigitalWalletServerHandlerLog digitalWalletServerHandlerLog(
            DigitalWalletServerHandler digitalWalletServerHandler) {
        return new DigitalWalletServerHandlerLog(digitalWalletServerHandler);
    }

    @Bean
    public PaymentServerHandlerMdcLog paymentServerHandlerLog(PaymentServerHandler paymentServerHandler) {
        return new PaymentServerHandlerMdcLog(new PaymentServerHandlerLog(paymentServerHandler));
    }

}
