package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.domain.CategoryRef;
import com.rbkmoney.damsel.domain.Contractor;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.domain.ShopDetails;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentParams;
import com.rbkmoney.damsel.payment_processing.ShopParams;
import com.rbkmoney.damsel.payment_processing.UserInfo;

public class PaymentProcessingWrapper {

    public static UserInfo makeUserInfo(String userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        return userInfo;
    }

    public static ShopParams makeShopParams(ShopDetails shopDetails, CategoryRef categoryRef, Contractor contractor) {
        ShopParams shopParams = new ShopParams();
        shopParams.setDetails(shopDetails);
        shopParams.setContractor(contractor);
        shopParams.setCategory(categoryRef);
        return shopParams;
    }

    public static ShopParams makeShopParams(ShopDetails shopDetails, CategoryRef categoryRef) {
        return PaymentProcessingWrapper.makeShopParams(shopDetails, categoryRef, null);
    }

    public static InvoicePaymentParams makeInvoicePaymentParams(Payer payer) {
        InvoicePaymentParams invoicePaymentParams = new InvoicePaymentParams();
        invoicePaymentParams.setPayer(payer);
        return invoicePaymentParams;
    }

}
