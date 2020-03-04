package com.rbkmoney.proxy.mocketbank.validator;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.proxy.mocketbank.exception.PaymentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractPaymentResource;
import static com.rbkmoney.proxy.mocketbank.utils.extractor.proxy.ProxyProviderPackageExtractors.extractPaymentTool;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        PaymentResource paymentResource = extractPaymentResource(context);
        PaymentTool paymentTool = extractPaymentTool(paymentResource);
        if (!paymentResource.isSetRecurrentPaymentResource() && !paymentTool.isSetBankCard()) {
            throw new PaymentException("Isn`t payment tool bank card");
        }
    }

}

