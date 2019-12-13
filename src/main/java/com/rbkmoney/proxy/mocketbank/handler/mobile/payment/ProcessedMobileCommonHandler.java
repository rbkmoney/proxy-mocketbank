package com.rbkmoney.proxy.mocketbank.handler.mobile.payment;


import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.extractor.ProxyProviderPackageExtractorsExtends;
import com.rbkmoney.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import com.rbkmoney.proxy.mocketbank.utils.PaymentUtils;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhoneAction;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhoneUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedMobileCommonHandler implements CommonMobileHandler {

    private final List<MobilePhone> mobilePhones;
    private final ErrorMapping errorMapping;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return (targetInvoicePaymentStatus.isSetProcessed());
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        PaymentTool paymentTool = ProxyProviderPackageExtractorsExtends.extractPaymentTool(paymentResource);
        String phoneNumber = MobilePhoneUtils.preparePhoneNumber(paymentTool.getMobileCommerce().getPhone());
        Optional<MobilePhone> mobilePhone = MobilePhoneUtils.extractPhoneByNumber(mobilePhones, phoneNumber);

        if (!mobilePhone.isPresent()) {
            String error = MobilePhoneAction.UNSUPPORTED_PHONE.getAction();
            return createProxyResultFailure(errorMapping.getFailureByCodeAndDescription(error, error));
        }

        MobilePhoneAction mobilePhoneAction = MobilePhoneAction.findByValue(mobilePhone.get().getAction());
        if (MobilePhoneAction.isFailedAction(mobilePhoneAction.getAction())) {
            String error = mobilePhoneAction.getAction();
            return createProxyResultFailure(errorMapping.getFailureByCodeAndDescription(error, error));
        }

        TransactionInfo transactionInfo = createTransactionInfo(
                PaymentUtils.generateTransactionId(context.getPaymentInfo()), Collections.emptyMap()
        );
        return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getState().getBytes(), transactionInfo);
    }

}