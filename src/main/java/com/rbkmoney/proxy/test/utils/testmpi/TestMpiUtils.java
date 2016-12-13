package com.rbkmoney.proxy.test.utils.testmpi;

import com.rbkmoney.damsel.proxy_provider.PaymentInfo;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;


public class TestMpiUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestMpiUtils.class);

    public final static String MESSAGE_ID = "messageId";
    public final static String PA_REQ = "PaReq";
    public final static String PA_RES = "PaRes";
    public final static String PA_REQ_CREATION_TIME = "paReqCreationTime";
    public final static String ACS_URL = "acsUrl";
    public final static String ACCT_ID = "acctId";
    public final static String PURCHASE_XID = "purchaseXId";

    public static String getCallbackUrl(String callbackUrl, String path) {
        String prepareCallbackUrl = null;
        try {
            URIBuilder b = new URIBuilder(callbackUrl);
            prepareCallbackUrl = b.setPath(path)
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            LOGGER.error("Exception in getCallbackUrl", e);
        }
        return prepareCallbackUrl;
    }

    public static String generateInvoice(PaymentInfo payment) {
        return payment.getInvoice().getId() + payment.getPayment().getId();
    }
}
