package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy.Intent;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.*;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiApi;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiUtils;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiEnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiTransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
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

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction.*;

@Component
public class MocketBankServerHandler implements ProviderProxySrv.Iface {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandler.class);

    private Map<String, String> options;

    @Autowired
    private CdsApi cds;

    @Autowired
    MocketBankMpiApi mocketBankMpiApi;

    @Value("${proxy-mocketbank.callbackUrl}")
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
    public ProxyResult processPayment(Context context) throws TException {

        options = (context.getOptions().size() > 0) ? context.getOptions() : new HashMap<>();

        TargetInvoicePaymentStatus target = context.getSession().getTarget();

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
        com.rbkmoney.damsel.proxy_provider.InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        String session = invoicePayment.getPayer().getSessionId();
        String token = invoicePayment.getPayer().getPaymentTool().getBankCard().getToken();
        CardData cardData;
        TransactionInfo transactionInfo = null;

        com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

        try {
            LOGGER.info("Processed: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("Processed: CDS Exception", e);
            return ProxyProviderWrapper.makeProxyResultFailure("Processed: CDS Exception", e.getMessage());
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());

        if (card.isPresent()) {
            MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());

            if (!cardUtils.isEnrolled(card)) {
                String error;
                switch (action) {
                    case INSUFFICIENT_FUNDS:
                        error = INSUFFICIENT_FUNDS.getAction();
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
                                MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                                Collections.emptyMap()
                        );
                        ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResult(
                                intent,
                                "captured".getBytes(),
                                transactionInfo
                        );
                        LOGGER.info("Processed: success {}", proxyResult);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResultFailure(error, error);
                LOGGER.info("Processed: failure {}", proxyResult);
                return ProxyProviderWrapper.makeProxyResultFailure(error, error);
            }

        } else {
            ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResultFailure(
                    UNSUPPORTED_CARD.getAction(),
                    UNSUPPORTED_CARD.getAction()
            );
            LOGGER.info("Processed: failure {}", proxyResult);
            return proxyResult;
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse = null;
        try {
            verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException e) {
            LOGGER.error("Processed: Exception in verifyEnrollment", e);
            return ProxyProviderWrapper.makeProxyResultFailure(
                    UNKNOWN_FAILURE.getAction(),
                    UNKNOWN_FAILURE.getAction()
            );
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = "MPI-" + MocketBankMpiUtils.generateInvoice(context.getPaymentInfo());
            LOGGER.info("Processed: suspend tag {}", tag);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            LOGGER.info("Processed: prepare redirect params {}", params);

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
        extra.put(MocketBankMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        LOGGER.info("Processed: Extra map {}", extra);

        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (IOException e) {

            LOGGER.error("Processed: Converter Exception", e);
            return ProxyProviderWrapper.makeProxyResultFailure("Converter", e.getMessage());
        }

        ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResult(intent, state, transactionInfo);
        LOGGER.info("Processed: finish {}", proxyResult);
        return proxyResult;
    }

    private ProxyResult captured(Context context) {
        LOGGER.info("Captured: start");
        com.rbkmoney.damsel.proxy_provider.InvoicePayment payment = context.getPaymentInfo().getPayment();
        TransactionInfo transactionInfoContractor = payment.getTrx();
        TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                transactionInfoContractor.getId(),
                transactionInfoContractor.getExtra()
        );

        context.getSession().setState("confirm".getBytes());

        Intent intent = ProxyWrapper.makeFinishIntentSuccess();
        ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResult(intent, "confirm".getBytes(), transactionInfo);

        LOGGER.info("Captured: proxyResult {}", proxyResult);
        return proxyResult;
    }

    private ProxyResult cancelled(Context context) {
        ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResultFailure("Unsupported method CANCEL", "Unsupported method CANCEL");
        LOGGER.error("Cancelled: proxyResult {}", proxyResult);
        return proxyResult;
    }

    @Override
    public CallbackResult handlePaymentCallback(ByteBuffer byteBuffer, Context context) throws TException {
        LOGGER.info("HandlePaymentCallback: start");
        InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        String token = invoicePayment.getPayer().getPaymentTool().getBankCard().getToken();
        String session = invoicePayment.getPayer().getSessionId();
        options = context.getOptions();

        HashMap<String, String> parameters;

        LOGGER.info("HandlePaymentCallback: merge input parameters");
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
        } catch (Exception e) {
            LOGGER.error("HandlePaymentCallback: Parse ByteBuffer Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: Parse ByteBuffer Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("HandlePaymentCallback: merge input parameters {}", parameters);

        CardData cardData;
        try {
            LOGGER.info("HandlePaymentCallback: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("HandlePaymentCallback: CDS Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: CDS Exception",
                    e.getMessage()
            );
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException e) {
            LOGGER.error("HandlePaymentCallback: Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("HandlePaymentCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

            TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                    MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                    Collections.emptyMap()
            );

            ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResult(
                    intent, "captured".getBytes(), transactionInfo
            );

            LOGGER.info("HandlePaymentCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return ProxyProviderWrapper.makeCallbackResult(callbackResponse, proxyResult);
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());
        MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());
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

        CallbackResult callbackResult = ProxyProviderWrapper.makeCallbackResultFailure(
                "error".getBytes(), "HandlePaymentCallback: error", error
        );

        LOGGER.info("HandlePaymentCallback: callbackResult {}", callbackResult);
        return callbackResult;
    }

}
