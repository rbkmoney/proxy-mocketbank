package com.rbkmoney.proxy.mocketbank.validator;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.exception.DigitalWalletException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalWalletValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        PaymentResource paymentResource =
                com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractPaymentResource(
                        context);
        PaymentTool paymentTool = ProxyProviderPackageExtractors.extractPaymentTool(paymentResource);
        if (!paymentTool.isSetDigitalWallet()) {
            throw new DigitalWalletException("Isn`t payment tool payment terminal");
        }
    }
}

