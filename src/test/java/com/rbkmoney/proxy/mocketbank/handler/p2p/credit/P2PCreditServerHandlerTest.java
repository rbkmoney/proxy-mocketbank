package com.rbkmoney.proxy.mocketbank.handler.p2p.credit;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.Currency;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.domain.*;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Cash;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.identdocstore.identity_document_storage.RussianDomesticPassport;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.handler.IntegrationBaseRule;
import com.rbkmoney.proxy.mocketbank.handler.p2p.P2PCreditServerHandler;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsIDStorageApi;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsStorageApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import org.apache.thrift.TException;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.url.keyring=http://127.0.0.1:8021/v1/keyring",
                "cds.url.storage=http://127.0.0.1:8021/v1/storage",
                "cds.url.idStorage=http://127.0.0.1:8021/v1/identity_document_storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore("Integration test")
public class P2PCreditServerHandlerTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ClassRule
    public final static IntegrationBaseRule rule = new IntegrationBaseRule();

    @Autowired
    protected CdsStorageApi cds;

    @Autowired
    protected CdsIDStorageApi cdsIDStorageApi;

    @Autowired
    protected P2PCreditServerHandler creditHandler;

    protected String WithdrawalId = "TWithId" + (int) (Math.random() * 50 + 1);


    @Test
    public void testPayment() throws TException {
        ProcessResult processResult = creditHandler.processWithdrawal(
                makeWithdrawal(),
                Value.str(""),
                getProxyOptions()
        );

        log.info("Response processWithdrawal {}", processResult.toString());

        assertTrue("processWithdrawal ", processResult.getIntent().getFinish().getStatus().isSetSuccess());
    }

    protected Withdrawal makeWithdrawal() throws TException {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setId(WithdrawalId);

        Destination destination = new Destination();

        PutCardDataResult putCardDataResponse = cdsPutCardData(TestData.makeCardData());
        destination.setBankCard(putCardDataResponse.getBankCard());
        withdrawal.setDestination(destination);


        // CASH - BEGIN
        Cash cash = new Cash();
        cash.setAmount(1000L);

        Currency currency = new Currency();
        currency.setName("Rubles");
        currency.setNumericCode((short) 643);
        currency.setSymbolicCode("RUB");
        currency.setExponent((short) 1);

        cash.setCurrency(currency);
        withdrawal.setBody(cash);
        // CASH - END

        Identity identity = new Identity();

        List<ContactDetail> contactDetailList = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPhoneNumber("9876543210");
        contactDetailList.add(contactDetail);
        identity.setContact(contactDetailList);


        RUSDomesticPassport rusDomesticPassport = new RUSDomesticPassport();
        String token = cdsPutIdentityDocument();
        System.out.println("TOKEN: " + token);
        rusDomesticPassport.setToken(token);

        IdentityDocument identityDocument = new IdentityDocument();
        identityDocument.setRusDomesticPassport(rusDomesticPassport);

        List<IdentityDocument> identityDocumentList = new ArrayList<>();
        identityDocumentList.add(identityDocument);

        identity.setDocuments(identityDocumentList);

        withdrawal.setSender(identity);


        return withdrawal;
    }

    protected Map<String, String> getProxyOptions() {
        Map<String, String> options = new HashMap<>();
        return options;
    }

    protected PutCardDataResult cdsPutCardData(CardData cardData) {
        log.info("CDS: put card request start");

        Auth3DS auth3DS = CdsWrapper.makeAuth3DS("jKfi3B417+zcCBFYbFp3CBUAAAA=", "5");
        AuthData authData = CdsWrapper.makeAuthDataWithAuth3DS(auth3DS);

        SessionData sessionData = CdsWrapper.makeSessionData(authData);

        PutCardDataResult putCardDataResponse = cds.putCardData(cardData, sessionData);
        log.info("CDS: put card response {}", putCardDataResponse);
        return putCardDataResponse;
    }

    protected String cdsPutIdentityDocument() throws TException {

        RussianDomesticPassport passport = new RussianDomesticPassport();
        passport.setSeries("series");
        passport.setNumber("number");
        passport.setIssuer("issuer");
        passport.setIssuerCode("issuer_code");
        passport.setIssuedAt("2016-03-22T06:12:27Z");
        passport.setFamilyName("Петров");
        passport.setFirstName("Николай");
        passport.setBirthDate("2016-03-22T06:12:27Z");
        passport.setBirthPlace("2016-03-22T06:12:27Z");

        com.rbkmoney.identdocstore.identity_document_storage.IdentityDocument document = new com.rbkmoney.identdocstore.identity_document_storage.IdentityDocument();
        document.setRussianDomesticPassport(passport);

        return cdsIDStorageApi.put(document);
    }

}
