package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import com.rbkmoney.damsel.proxy_provider.PaymentInfo;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class MocketBankMpiUtils {

    public final static String MASK_CHAR = "*";

    public final static String MESSAGE_ID = "messageId";
    public final static String PA_REQ = "PaReq";
    public final static String PA_RES = "PaRes";
    public final static String PA_REQ_CREATION_TIME = "paReqCreationTime";
    public final static String ACS_URL = "acsUrl";
    public final static String ACCT_ID = "acctId";
    public final static String PURCHASE_XID = "purchaseXId";

    public static String getCallbackUrl(String callbackUrl, String path) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .build()
                .toUriString();
    }

    public static String generateInvoice(PaymentInfo payment) {
        return payment.getInvoice().getId() + payment.getPayment().getId();
    }

    public static String maskNumber(final String creditCardNumber, int startLength, int endLength, String maskChar) {
        final String cardNumber = creditCardNumber.replaceAll("\\D", "");

        final int start = startLength;
        final int end = cardNumber.length() - endLength;
        final String overlay = StringUtils.repeat(maskChar, end - start);

        return StringUtils.overlay(cardNumber, overlay, start, end);
    }

    public static String maskNumber(final String creditCardNumber) {
        return maskNumber(creditCardNumber, 4, 4, MASK_CHAR);
    }
}
