package com.rbkmoney.proxy.mocketbank.handler.mobile.payment;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.utils.creator.ProxyProviderCreator;
import com.rbkmoney.proxy.mocketbank.utils.extractor.proxy.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhoneAction;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedMobileCommonHandler implements CommonMobileHandler {

    private final List<MobilePhone> mobilePhones;
    private final ErrorMapping errorMapping;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        PaymentResource paymentResource = com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractPaymentResource(context);
        PaymentTool paymentTool = ProxyProviderPackageExtractors.extractPaymentTool(paymentResource);
        String phoneNumber = MobilePhoneUtils.preparePhoneNumber(paymentTool.getMobileCommerce().getPhone());
        Optional<MobilePhone> mobilePhone = MobilePhoneUtils.extractPhoneByNumber(mobilePhones, phoneNumber);

        if (!mobilePhone.isPresent()) {
            String error = MobilePhoneAction.UNSUPPORTED_PHONE.getAction();
            return createProxyResultFailure(errorMapping.mapFailure(error, error));
        }

        MobilePhoneAction mobilePhoneAction = MobilePhoneAction.findByValue(mobilePhone.get().getAction());
        if (MobilePhoneAction.isFailedAction(mobilePhoneAction.getAction())) {
            String error = mobilePhoneAction.getAction();
            return createProxyResultFailure(errorMapping.mapFailure(error, error));
        }

        TransactionInfo transactionInfo = ProxyProviderCreator.createDefaultTransactionInfo(context);
        return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo);
    }

}
