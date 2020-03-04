package com.rbkmoney.proxy.mocketbank.service.mpi.model;

import com.rbkmoney.proxy.mocketbank.service.mpi.constant.MpiField;
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
        prepareFields.add(MpiField.PAN.getValue(), pan);
        prepareFields.add(MpiField.PARES.getValue(), paRes);
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
