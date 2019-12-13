package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;

import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiRequestField;
import com.rbkmoney.proxy.mocketbank.utils.model.CreditCardUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@Builder
@RequiredArgsConstructor
public class ValidatePaResRequest implements PrepareFieldsObject {

    private final String pan;
    private final String paRes;

    @Override
    public MultiValueMap<String, Object> prepareFields() {
        MultiValueMap<String, Object> prepareFields = new LinkedMultiValueMap<>();
        prepareFields.add(MpiRequestField.PAN.getValue(), pan);
        prepareFields.add(MpiRequestField.PARES.getValue(), paRes);
        return prepareFields;
    }

    @Override
    public String toString() {
        return "ValidatePaResRequest{" +
                "pan='" + CreditCardUtils.maskNumber(pan) + '\'' +
                ", paRes='" + paRes + '\'' +
                '}';
    }

}
