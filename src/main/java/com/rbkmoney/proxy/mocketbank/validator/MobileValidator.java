package com.rbkmoney.proxy.mocketbank.validator;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.exception.MobileException;
import com.rbkmoney.proxy.mocketbank.extractor.ProxyProviderPackageExtractorsExtends;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MobileValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        PaymentTool paymentTool = ProxyProviderPackageExtractorsExtends.extractPaymentTool(paymentResource);
        if (!paymentTool.isSetMobileCommerce()) {
            throw new MobileException("Isn`t payment tool mobile commerce");
        }
    }

}

