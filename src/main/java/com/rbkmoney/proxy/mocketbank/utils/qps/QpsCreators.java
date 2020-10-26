package com.rbkmoney.proxy.mocketbank.utils.qps;

import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.damsel.user_interaction.QrCode;
import com.rbkmoney.damsel.user_interaction.QrCodeDisplayRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QpsCreators {

    public static QrCodeDisplayRequest createQrCodeDisplayRequest(String payload) {
        return new QrCodeDisplayRequest().setQrCode(new QrCode().setPayload(payload.getBytes()));
    }

    public static MultiValueMap<String, String> createQpsParams(String invoiceId, Cash cash) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", invoiceId);
        params.put("type", "02");
        params.put("bank", "100000000000");
        params.put("sum", String.format("%d", cash.getAmount()));
        params.put("cur", cash.getCurrency().getSymbolicCode());
        params.put("crc", "AB75");

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(params);
        return multiValueMap;
    }

}
