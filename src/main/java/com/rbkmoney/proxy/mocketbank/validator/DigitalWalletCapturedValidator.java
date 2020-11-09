package com.rbkmoney.proxy.mocketbank.validator;

import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.proxy.mocketbank.exception.PaymentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalWalletCapturedValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        if (!context.getPaymentInfo().isSetCapture()) {
            throw new PaymentException("Captured not set");
        }
    }

}

