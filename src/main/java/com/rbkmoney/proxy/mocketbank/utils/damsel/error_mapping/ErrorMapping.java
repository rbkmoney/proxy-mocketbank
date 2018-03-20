package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.model.Error;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


/**
 * @author Anatoly Cherkasov
 */
@Component
public class ErrorMapping {

    private static List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errors;

    @Autowired
    public ErrorMapping(List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errorList) {
        ErrorMapping.errors = errorList;
    }

    public static Failure getFailureByCodeAndDescription(String code, String description) {
        com.rbkmoney.proxy.mocketbank.utils.model.Error error = findMatchWithPattern(errors, code, description);

        Failure failure = toGeneral(error.getMapping());
        failure.setReason(prepareReason(code, description));
        return failure;
    }

    public static com.rbkmoney.proxy.mocketbank.utils.model.Error findMatchWithPattern(
            List<Error> errors,
            String code,
            String description
    ) {
        if (code == null || description == null) {
            throw new IllegalArgumentException();
        }

        return errors.stream()
                .filter(error -> (code.matches(error.getRegexp())
                        && description.matches(error.getRegexp())
                ))
                .findFirst()
                .orElseThrow(() -> new WUndefinedResultException("Mocketbank. Undefined error. code " + code + ", description " + description));
    }

    private static String prepareReason(String code, String description) {
                return String.format("'%s' - '%s'", code, description);
    }

}
