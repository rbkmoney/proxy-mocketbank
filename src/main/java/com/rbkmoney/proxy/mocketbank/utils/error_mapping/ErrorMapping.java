package com.rbkmoney.proxy.mocketbank.utils.error_mapping;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.model.Error;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


/**
 * @author Anatoly Cherkasov
 */
@Component
public class ErrorMapping {

    private static List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errors;

    private static final String PATTERN_REASON_DEFAULT = "'%s' - '%s'";

    /**
     * Application name
     */
    private static String applicationName;

    /**
     * Pattern for reason failure
     */
    private static String patternReason;

    /**
     * Constructs a new {@link ErrorMapping} instance.
     */
    public ErrorMapping() {
        // Constructs default a new {@link ErrorMapping} instance.
    }


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ErrorMapping} instance with the given
     * initial parameters to be constructed.
     *
     * @param errorList the field's errors (see {@link #errors}).
     */
    @Autowired
    public ErrorMapping(
            final List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errorList,
            final @Value("${error-mapping.name:${spring.application.name}}") String applicationName,
            final @Value("${error-mapping.patternReason:" + PATTERN_REASON_DEFAULT + "}") String patternReason
    ) {
        ErrorMapping.errors = errorList;
        ErrorMapping.applicationName = applicationName;
        ErrorMapping.patternReason = patternReason;
    }


    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /**
     * Get failure by code and description
     *
     * @param code        String
     * @param description String
     * @return Failure
     */
    public static Failure getFailureByCodeAndDescription(String code, String description) {
        com.rbkmoney.proxy.mocketbank.utils.model.Error error = findMatchWithPattern(errors, code, description);

        Failure failure = toGeneral(error.getMapping());
        failure.setReason(prepareReason(code, description));
        return failure;
    }

    /**
     * Find match code or description by pattern
     *
     * @param errors      List<Error>
     * @param code        String
     * @param description String
     * @return com.rbkmoney.proxy.mocketbank.utils.model.Error
     */
    public static com.rbkmoney.proxy.mocketbank.utils.model.Error findMatchWithPattern(
            List<Error> errors,
            String code,
            String description
    ) {
        if (code == null || description == null) {
            throw new IllegalArgumentException();
        }

        return errors.stream()
                .filter(error ->
                        (code.matches(error.getRegexp())
                                || description.matches(error.getRegexp())
                        )
                )
                .findFirst()
                .orElseThrow(() -> new WUndefinedResultException(String.format("%s. Undefined error. code %s, description %s", applicationName, code, description)));
    }


    // ------------------------------------------------------------------------
    // Private methods
    // ------------------------------------------------------------------------

    /**
     * Prepare reason for {@link Failure}
     *
     * @param code        String
     * @param description String
     * @return String
     */
    private static String prepareReason(String code, String description) {
        return String.format(patternReason, code, description);
    }

}
