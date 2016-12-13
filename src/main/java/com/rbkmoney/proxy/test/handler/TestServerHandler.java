package com.rbkmoney.proxy.test.handler;

import com.rbkmoney.damsel.base.TryLater;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy.Intent;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.test.utils.CardUtils;
import com.rbkmoney.proxy.test.utils.Converter;
import com.rbkmoney.proxy.test.utils.cds.CdsApi;
import com.rbkmoney.proxy.test.utils.damsel.*;
import com.rbkmoney.proxy.test.utils.model.Card;
import com.rbkmoney.proxy.test.utils.testmpi.TestMpiApi;
import com.rbkmoney.proxy.test.utils.testmpi.TestMpiUtils;
import com.rbkmoney.proxy.test.utils.testmpi.constant.TestMpiAction;
import com.rbkmoney.proxy.test.utils.testmpi.constant.TestMpiEnrollmentStatus;
import com.rbkmoney.proxy.test.utils.testmpi.constant.TestMpiTransactionStatus;
import com.rbkmoney.proxy.test.utils.testmpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.test.utils.testmpi.model.VerifyEnrollmentResponse;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.proxy.test.utils.testmpi.constant.TestMpiAction.*;

@Component
public class TestServerHandler implements ProviderProxySrv.Iface {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestServerHandler.class);

    private Map<String, String> options;

    @Autowired
    private CdsApi cds;

    @Autowired
    TestMpiApi testMpiApi;

    @Value("${proxy-test.callbackUrl}")
    private String callbackUrl;

    @Value("${fixture.cards}")
    private Resource fixtureCards;

    @Value("${timer.timeout}")
    private int timerTimeout;

    private List<Card> cardList;

    @PostConstruct
    public void init() throws IOException {
        cardList = CardUtils.getCardListFromFile(fixtureCards.getInputStream());
    }

    @Override
    public ProxyResult processPayment(Context context) throws TryLater, TException {

        options = (context.getOptions().size() > 0) ? context.getOptions() : new HashMap<>();

        Target target = context.getSession().getTarget();

        if (target.isSetProcessed()) {
            return processed(context);
        } else if (target.isSetCaptured()) {
            return captured(context);
        } else if (target.isSetCancelled()) {
            return cancelled(context);
        } else {
            LOGGER.error("Error unsupported method");
            return ProxyProviderWrapper.makeProxyResultFailure("Unsupported method", "Unsupported method");
        }

    }

    private ProxyResult processed(Context context) {
        LOGGER.info("processed start");
        com.rbkmoney.damsel.proxy_provider.InvoicePayment invoicePayment = context.getPayment().getPayment();
        String session = invoicePayment.getPayer().getSession();
        String token = invoicePayment.getPayer().getPaymentTool().getBankCard().getToken();
        CardData cardData;
        TransactionInfo transactionInfo = null;

        com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentOk();

        try {
            LOGGER.info("Call CDS in processPayment. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("CDS Exception in processPayment", e);
            return ProxyProviderWrapper.makeProxyResultFailure("CDS Exception in processPayment", e.getMessage());
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());

        if (card.isPresent()) {
            TestMpiAction action = TestMpiAction.findByValue(card.get().getAction());

            if (!cardUtils.isEnrolled(card)) {
                String error = null;
                switch (action) {
                    case INCUFFICIENT_FUNDS:
                        error = INCUFFICIENT_FUNDS.getAction();
                        break;
                    case INVALID_CARD:
                        error = INVALID_CARD.getAction();
                        break;
                    case CVV_MATCH_FAIL:
                        error = CVV_MATCH_FAIL.getAction();
                        break;
                    case EXPIRED_CARD:
                        error = EXPIRED_CARD.getAction();
                        break;
                    case SUCCESS:
                        transactionInfo = DomainWrapper.makeTransactionInfo(
                                TestMpiUtils.generateInvoice(context.getPayment()),
                                Collections.emptyMap()
                        );
                        return ProxyProviderWrapper.makeProxyResult(
                                intent,
                                "captured".getBytes(),
                                transactionInfo
                        );
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                return ProxyProviderWrapper.makeProxyResultFailure(
                        error,
                        error
                );
            }

        } else {
            return ProxyProviderWrapper.makeProxyResultFailure(
                    UNSUPPORTED_CARD.getAction(),
                    UNSUPPORTED_CARD.getAction()
            );
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse = null;
        try {
            verifyEnrollmentResponse = testMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException e) {
            LOGGER.error("Exception in verifyEnrollment", e);
            return ProxyProviderWrapper.makeProxyResultFailure(
                    UNKNOWN_FAILURE.getAction(),
                    UNKNOWN_FAILURE.getAction()
            );
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(TestMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {

            String tag = "MPI-" + TestMpiUtils.generateInvoice(context.getPayment());

            // Prepare response
            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", TestMpiUtils.getCallbackUrl(callbackUrl, "/test/term_url{?termination_uri}"));

            intent = ProxyWrapper.makeIntentWithSuspendIntent(
                    tag, BaseWrapper.makeTimerTimeout(timerTimeout),
                    UserInteractionWrapper.makeUserInteraction(
                            UserInteractionWrapper.makeBrowserPostRequest(
                                    url,
                                    params
                            )
                    )
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(TestMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        LOGGER.info("Extra map {}", extra);

        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (IOException e) {

            LOGGER.error("Converter Exception in processPayment", e);
            return ProxyProviderWrapper.makeProxyResultFailure("Converter", e.getMessage());
        }

        return ProxyProviderWrapper.makeProxyResult(intent, state, transactionInfo);
    }

    private ProxyResult captured(Context context) {
        LOGGER.info("captured start");
        com.rbkmoney.damsel.proxy_provider.InvoicePayment payment = context.getPayment().getPayment();
        TransactionInfo transactionInfoContractor = payment.getTrx();
        TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                transactionInfoContractor.getId(),
                transactionInfoContractor.getExtra()
        );

        context.getSession().setState("confirm".getBytes());

        Intent intent = ProxyWrapper.makeFinishIntentOk();
        LOGGER.info("capturePayment finish");
        return ProxyProviderWrapper.makeProxyResult(intent, "confirm".getBytes(), transactionInfo);
    }

    private ProxyResult cancelled(Context context) {
        return ProxyProviderWrapper.makeProxyResultFailure("Unsupported method CANCEL", "Unsupported method CANCEL");
    }

    @Override
    public CallbackResult handlePaymentCallback(ByteBuffer byteBuffer, Context context) throws TryLater, TException {
        LOGGER.info("handlePaymentCallback start");
        InvoicePayment invoicePayment = context.getPayment().getPayment();
        options = context.getOptions();

        HashMap<String, String> parameters;

        // Merge parameters
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
        } catch (Exception e) {
            LOGGER.error("Parse ByteBuffer Exception in handlePaymentCallback", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "Parse ByteBuffer Exception in handlePaymentCallback",
                    e.getMessage()
            );
        }

        CardData cardData;
        try {
            LOGGER.info("CDS: handlePaymentCallback get Card Data");
            cardData = cds.getSessionCardData(
                    invoicePayment.getPayer().getPaymentTool().getBankCard().getToken(),
                    invoicePayment.getPayer().getSession()
            );
        } catch (TException e) {
            LOGGER.error("CDS Exception in handlePaymentCallback", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "CDS Exception in handlePaymentCallback",
                    e.getMessage()
            );
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = testMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException e) {
            LOGGER.error("Exception in handlePaymentCallback", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "Exception in handlePaymentCallback",
                    e.getMessage()
            );
        }

        LOGGER.info("validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(TestMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentOk();

            TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                    TestMpiUtils.generateInvoice(context.getPayment()),
                    Collections.emptyMap()
            );

            return ProxyProviderWrapper.makeCallbackResult(
                    callbackResponse,
                    ProxyProviderWrapper.makeProxyResult(
                            intent, "captured".getBytes(), transactionInfo
                    )
            );
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());


        TestMpiAction action = TestMpiAction.findByValue(card.get().getAction());

        String error;

        switch (action) {
            case THREE_D_SECURE_FAILURE:
                error = THREE_D_SECURE_FAILURE.getAction();
                break;
            case THREE_D_SECURE_TIMEOUT:
                error = THREE_D_SECURE_TIMEOUT.getAction();
                break;
            default:
                error = UNKNOWN_FAILURE.getAction();

        }

        LOGGER.info("handlePaymentCallback finish");
        return ProxyProviderWrapper.makeCallbackResultFailure(
                "error".getBytes(),
                "Exception in handlePaymentCallback",
                error
        );
    }

}
