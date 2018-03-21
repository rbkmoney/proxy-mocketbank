package com.rbkmoney.proxy.mocketbank.utils.error_mapping;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.model.Error;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;

import java.util.List;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


/**
 * @author Anatoly Cherkasov
 */
public class ErrorMapping {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final String PATTERN_REASON_DEFAULT = "'%s' - '%s'";

    private static ErrorMapping INSTANCE;

    /**
     * Application name
     */
    private String applicationName;

    /**
     * Pattern for reason failure
     */
    private String patternReason;

    /**
     * List of errors
     */
    private List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ErrorMapping} instance.
     */
    private ErrorMapping() {
        // By default, a new instance is not created, use getInstance
    }

    public static ErrorMapping getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ErrorMapping();
        }
        return INSTANCE;
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
    public Failure getFailureByCodeAndDescription(String code, String description) {
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
    private com.rbkmoney.proxy.mocketbank.utils.model.Error findMatchWithPattern(
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
    private String prepareReason(String code, String description) {
        return String.format(this.patternReason, code, description);
    }


    // ------------------------------------------------------------------------
    // Getter and Setter methods
    // ------------------------------------------------------------------------

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPatternReason() {
        return patternReason;
    }

    public void setPatternReason(String patternReason) {
        this.patternReason = patternReason;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

}
