package com.rbkmoney.proxy.mocketbank.utils.dw;

import com.rbkmoney.damsel.domain.DigitalWallet;
import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DwCreators {

    public static HashMap<String, String> createDWParams(
            String invoiceId,
            Cash cash,
            DigitalWallet digitalWallet,
            AdapterMockBankProperties mockBankProperties
    ) {
        HashMap<String, String> params = new HashMap<>();
        params.put("shop", "test");
        params.put("bill_id", invoiceId);

        String url = UrlUtils.getCallbackUrl(
                mockBankProperties.getCallbackUrl(),
                mockBankProperties.getPathDWCallbackUrl()
        );
        params.put("success_url", "{termination_uri}");
        params.put("fail_url", "{termination_uri}");
        params.put("callback_url", url);

        String amount = String.valueOf(cash.getAmount());
        params.put("amount", amount);

        String user = (digitalWallet == null) ? "" : digitalWallet.getId();
        params.put("user", "tel:" + user);
        return params;
    }

}
