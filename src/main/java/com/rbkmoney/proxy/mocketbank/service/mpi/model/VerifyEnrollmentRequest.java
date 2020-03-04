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
public class VerifyEnrollmentRequest implements PrepareFieldsObject {

    private final String pan;
    private final short year;
    private final byte month;

    @Override
    public MultiValueMap<String, Object> prepareFields() {
        MultiValueMap<String, Object> prepareFields = new LinkedMultiValueMap<>();
        prepareFields.add(MpiField.PAN.getValue(), pan);
        prepareFields.add(MpiField.YEAR.getValue(), String.valueOf(year));
        prepareFields.add(MpiField.MONTH.getValue(), String.valueOf(month));
        return prepareFields;
    }

    @Override
    public String toString() {
        return "VerifyEnrollmentRequest{" +
                "pan='" + CreditCardUtils.maskNumber(pan) + '\'' +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                '}';
    }
}
