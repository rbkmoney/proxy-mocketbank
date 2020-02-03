package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.identity_document_storage.IdentityDocumentStorageSrv;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.domain.*;
import com.rbkmoney.damsel.withdrawals.provider_adapter.AdapterSrv;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Cash;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.handler.IntegrationBaseRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.damsel.identity_document_storage.IdentityDocument.russian_domestic_passport;
import static com.rbkmoney.java.damsel.utils.creators.CdsPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createCurrency;
import static com.rbkmoney.java.damsel.utils.extractors.WithdrawalsProviderAdapterPackageExtractors.isSuccess;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.identity-document-storage.url=http://127.0.0.1:8021/v1/identity_document_storage",
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore("Integration test")
public class OctServerHandlerTest {

    @ClassRule
    public final static IntegrationBaseRule rule = new IntegrationBaseRule();

    @Autowired
    protected com.rbkmoney.damsel.cds.StorageSrv.Iface cds;

    @Autowired
    protected IdentityDocumentStorageSrv.Iface cdsIDStorageApi;

    @Autowired
    protected AdapterSrv.Iface handler;

    private String WithdrawalId = "TWithId" + (int) (Math.random() * 50 + 1);
    private String phoneNumber = "9876543210";

    @Test
    public void testProcessWithdrawal() throws TException {
        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(), Value.str(""), createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue("Result processWithdrawal isn`t success", isSuccess(result));
    }

    private Withdrawal createWithdrawal() throws TException {
        PutCardDataResult putCardDataResponse = cdsPutCardData(TestData.makeCardData());
        Destination destination = createDestination(putCardDataResponse);

        String token = cdsPutIdentityDocument();
        List<IdentityDocument> identityDocumentList = createIdentityDocumentsList(token);
        Identity identity = createIdentity(identityDocumentList);

        return new Withdrawal()
                .setId(WithdrawalId)
                .setDestination(destination)
                .setBody(createCash())
                .setSender(identity);
    }

    private List<IdentityDocument> createIdentityDocumentsList(String token) {
        List<IdentityDocument> identityDocumentList = new ArrayList<>();
        identityDocumentList.add(createIdentityDocument(token));
        return identityDocumentList;
    }

    private Destination createDestination(PutCardDataResult putCardDataResponse) {
        Destination destination = new Destination();
        destination.setBankCard(putCardDataResponse.getBankCard());
        return destination;
    }

    private Identity createIdentity(List<IdentityDocument> identityDocumentList) {
        return new Identity()
                .setContact(createContactDetailsList())
                .setDocuments(identityDocumentList);
    }

    private IdentityDocument createIdentityDocument(String token) {
        IdentityDocument identityDocument = new IdentityDocument();
        identityDocument.setRusDomesticPassport(new RUSDomesticPassport()
                .setToken(token));
        return identityDocument;
    }

    private PutCardDataResult cdsPutCardData(CardData cardData) throws TException {
        log.info("CDS: put card request start");
        Auth3DS auth3DS = createAuth3DS("jKfi3B417+zcCBFYbFp3CBUAAAA=", "5");
        AuthData authData = createAuthDataWithAuth3DS(auth3DS);
        SessionData sessionData = createSessionData(authData);
        PutCardDataResult putCardDataResponse = cds.putCardData(cardData, sessionData);
        putCardDataResponse.getBankCard().setExpDate(TestData.makeBankCard().getExpDate());
        log.info("CDS: put card response {}", putCardDataResponse);
        return putCardDataResponse;
    }

    private String cdsPutIdentityDocument() throws TException {
        com.rbkmoney.damsel.identity_document_storage.RussianDomesticPassport passport = new com.rbkmoney.damsel.identity_document_storage.RussianDomesticPassport();
        passport.setSeries("series")
                .setNumber("number")
                .setIssuer("issuer")
                .setIssuerCode("issuer_code")
                .setIssuedAt("2016-03-22T06:12:27Z")
                .setFamilyName("Петров")
                .setFirstName("Николай")
                .setBirthDate("2016-03-22T06:12:27Z")
                .setBirthPlace("2016-03-22T06:12:27Z");
        return cdsIDStorageApi.put(russian_domestic_passport(passport));
    }

    private static Cash createCash() {
        return new Cash()
                .setAmount(1000L)
                .setCurrency(createCurrency("Rubles", (short) 643, "RUB", (short) 1));
    }

    private Map<String, String> createProxyOptions() {
        return Collections.emptyMap();
    }

    private List<ContactDetail> createContactDetailsList() {
        List<ContactDetail> contactDetailList = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPhoneNumber(phoneNumber);
        contactDetailList.add(contactDetail);
        return contactDetailList;
    }

}
