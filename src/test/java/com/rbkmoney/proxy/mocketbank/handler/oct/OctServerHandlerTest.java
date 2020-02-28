package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.domain.*;
import com.rbkmoney.damsel.withdrawals.provider_adapter.AdapterSrv;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Cash;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.TestData;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
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

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createCurrency;
import static com.rbkmoney.java.damsel.utils.extractors.WithdrawalsProviderAdapterPackageExtractors.isSuccess;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.identity-document-storage.url=http://127.0.0.1:8021/v1/identity_document_storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerTest {

    @Autowired
    protected AdapterSrv.Iface handler;

    private String WithdrawalId = "TWithId" + (int) (Math.random() * 50 + 1);

    @Test
    public void testProcessWithdrawal() throws TException {
        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(),
                Value.str(""),
                createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue("Result processWithdrawal isn`t success", isSuccess(result));
    }

    private Withdrawal createWithdrawal() {
        BankCard bankCard = TestData.createBankCard(TestData.createCardData());
        Destination destination = createDestination(bankCard);

        List<IdentityDocument> identityDocumentList = createIdentityDocumentsList(TestData.WITHDRAWAL_TOKEN);
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

    private Destination createDestination(BankCard bankCard) {
        Destination destination = new Destination();
        destination.setBankCard(bankCard);
        return destination;
    }

    private Identity createIdentity(List<IdentityDocument> identityDocumentList) {
        return new Identity()
                .setContact(createContactDetailsList())
                .setDocuments(identityDocumentList);
    }

    private IdentityDocument createIdentityDocument(String token) {
        return IdentityDocument.rus_domestic_passport(new RUSDomesticPassport().setToken(token));
    }

    private Cash createCash() {
        return new Cash()
                .setAmount(1000L)
                .setCurrency(createCurrency("Rubles", (short) 643, "RUB", (short) 2));
    }

    private Map<String, String> createProxyOptions() {
        return Collections.emptyMap();
    }

    private List<ContactDetail> createContactDetailsList() {
        List<ContactDetail> contactDetailList = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPhoneNumber(TestData.PHONE_NUMBER);
        contactDetailList.add(contactDetail);
        return contactDetailList;
    }

}
