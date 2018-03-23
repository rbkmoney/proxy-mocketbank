package com.rbkmoney.proxy.mocketbank.utils.error_mapping;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


/**
 * @author Anatoly Cherkasov
 */
public class ErrorMapping {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String DEFAULT_PATTERN_REASON = "'%s' - '%s'";


    /**
     * Pattern for reason failure
     */
    private final String patternReason;

    /**
     * List of errors
     */
    private final List<Error> errors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------


    public ErrorMapping(InputStream inputStream) {
        this(inputStream, DEFAULT_PATTERN_REASON);
    }

    public ErrorMapping(InputStream inputStream, String patternReason) {
        this(inputStream, patternReason, new ObjectMapper());
    }

    public ErrorMapping(InputStream inputStream, String patternReason, ObjectMapper objectMapper) {
        this(patternReason, initErrorList(inputStream, objectMapper));
    }

    public ErrorMapping(String patternReason, List<Error> errors) {
        this.patternReason = patternReason;
        this.errors = errors;
    }

    public static List<Error> initErrorList(InputStream inputStream, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(inputStream, new TypeReference<List<Error>>() {});
        } catch (JsonParseException e) {
            throw new ErrorMappingException("Json can't parse data from file", e);
        } catch (JsonMappingException e) {
            throw new ErrorMappingException("Json can't mapping data from file", e);
        } catch (IOException e) {
            throw new ErrorMappingException("Failed to initErrorList", e);
        }
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
        Error error = findMatchWithPattern(errors, code, description);

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
    private Error findMatchWithPattern(
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
                .orElseThrow(() -> new WUndefinedResultException(String.format("Undefined error. code %s, description %s", code, description)));
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

    /**
     * Validate mapping formate
     */
    public void validateMappingFormate() {
        try {
            errors.forEach(error -> StandardError.findByValue(error.getMapping()));
        } catch (IllegalStateException ex) {
            log.error("Exception: validate error mapping", ex);
            System.exit(0);
        }
    }

}
