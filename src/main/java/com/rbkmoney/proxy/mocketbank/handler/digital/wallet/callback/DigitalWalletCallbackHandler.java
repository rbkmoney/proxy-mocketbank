package com.rbkmoney.proxy.mocketbank.handler.digital.wallet.callback;

import com.rbkmoney.damsel.proxy_provider.PaymentCallbackResult;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalWalletCallbackHandler {

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        return createCallbackResult("".getBytes(), createCallbackProxyResult(
                createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(),
                CreatorUtils.createDefaultTransactionInfo(context)
        ));
    }

}
