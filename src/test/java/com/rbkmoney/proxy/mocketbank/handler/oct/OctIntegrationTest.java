package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.withdrawals.domain.*;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Cash;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.decorator.WithdrawalServerHandlerLog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createCurrency;

public abstract class OctIntegrationTest {

    @Autowired
    protected WithdrawalServerHandlerLog handler;

    protected String WITHDRAWALID = "TEST_WITHDRAWAL_ID";


    protected Withdrawal createWithdrawal() {
        BankCard bankCard = TestData.createBankCard(TestData.createCardData());
        Destination destination = createDestination(bankCard);

        List<IdentityDocument> identityDocumentList = createIdentityDocumentsList(TestData.WITHDRAWAL_TOKEN);
        Identity identity = createIdentity(identityDocumentList);
        return new Withdrawal()
                .setId(WITHDRAWALID)
                .setDestination(destination)
                .setBody(createCash())
                .setSender(identity);
    }

    protected List<IdentityDocument> createIdentityDocumentsList(String token) {
        List<IdentityDocument> identityDocumentList = new ArrayList<>();
        identityDocumentList.add(createIdentityDocument(token));
        return identityDocumentList;
    }

    protected Destination createDestination(BankCard bankCard) {
        Destination destination = new Destination();
        destination.setBankCard(bankCard);
        return destination;
    }

    protected Identity createIdentity(List<IdentityDocument> identityDocumentList) {
        return new Identity()
                .setContact(createContactDetailsList())
                .setDocuments(identityDocumentList);
    }

    protected IdentityDocument createIdentityDocument(String token) {
        return IdentityDocument.rus_domestic_passport(new RUSDomesticPassport().setToken(token));
    }

    protected Cash createCash() {
        return new Cash()
                .setAmount(1000L)
                .setCurrency(createCurrency("Rubles", (short) 643, "RUB", (short) 2));
    }

    protected Map<String, String> createProxyOptions() {
        return Collections.emptyMap();
    }

    protected List<ContactDetail> createContactDetailsList() {
        List<ContactDetail> contactDetailList = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPhoneNumber(TestData.PHONE_NUMBER);
        contactDetailList.add(contactDetail);
        return contactDetailList;
    }

}
